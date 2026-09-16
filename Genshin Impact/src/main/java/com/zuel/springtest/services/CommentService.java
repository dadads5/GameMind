package com.zuel.springtest.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.PageResult;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.Comment;
import com.zuel.springtest.entity.Post;
import com.zuel.springtest.mapper.CommentLikeMapper;
import com.zuel.springtest.mapper.CommentMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.mapper.UserMapper;
import com.zuel.springtest.services.PostService;
import com.zuel.springtest.vo.CommentVO;
import com.zuel.springtest.vo.LikeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 评论业务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    /** 单页最大条数，防止 size 被放大后退化成全量查询 */
    private static final int MAX_PAGE_SIZE = 100;

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final StatisticsService statisticsService;
    private final PostService postService;

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    /**
     * 帖子下的评论列表（按发布时间正序，分页）
     *
     * <p>热帖的评论可能成百上千条，必须分页，否则单次请求会一次性 JOIN 出全量评论。
     */
    public PageResult<CommentVO> listByPost(Long postId, Long currentUserId, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 20;
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        PageHelper.startPage(page, size);
        List<Comment> comments = commentMapper.selectByPostId(postId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        fillLiked(pageInfo.getList(), currentUserId);
        return PageResult.of(pageInfo).map(CommentVO::from);
    }

    /**
     * 某用户发表的评论（按发布时间倒序，带帖子标题）
     */
    public PageResult<CommentVO> listByUser(Long userId, int page, int size, Long currentUserId) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        PageHelper.startPage(page, size);
        List<Comment> comments = commentMapper.selectByUserId(userId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);
        fillLiked(pageInfo.getList(), currentUserId);
        return PageResult.of(pageInfo).map(CommentVO::from);
    }

    public CommentVO getById(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        return CommentVO.from(comment);
    }

    // ------------------------------------------------------------------
    // 写操作
    // ------------------------------------------------------------------

    /**
     * 发表评论
     */
    @Transactional
    public CommentVO create(Long postId, Long userId, String content) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());

        if (commentMapper.insert(comment) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "发表评论失败");
        }

        // 重算帖子评论数
        postMapper.refreshCommentCount(postId);

        // 评论数 +1：热榜分数同步增加
        postService.incrementHotScore(postId, 5.0);

        // 通知帖子作者（自己评论自己的帖子不通知）
        notificationService.notifyCommentPost(post, userId, content);

        // 补充作者信息，保证新建结果可直接渲染
        userMapper.selectById(userId).ifPresent(user -> {
            comment.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            comment.setAuthorAvatar(user.getAvatar());
        });
        comment.setLiked(false);
        statisticsService.evict();
        return CommentVO.from(comment);
    }

    /**
     * 删除评论
     *
     * <p>评论作者本人，或帖子作者（版主视角）均可删除。
     */
    @Transactional
    public void delete(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }

        Post post = postMapper.selectById(comment.getPostId());
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isPostAuthor = post != null && post.getUserId().equals(userId);

        if (!isCommentAuthor && !isPostAuthor) {
            throw new BusinessException(ResultCode.FORBIDDEN, "没有权限删除该评论");
        }

        commentLikeMapper.deleteByCommentId(id);
        commentMapper.deleteById(id);

        if (post != null) {
            postMapper.refreshCommentCount(comment.getPostId());
            // 评论数 -1：热榜分数同步扣减
            postService.incrementHotScore(comment.getPostId(), -5.0);
        }
        log.info("评论 {} 已被用户 {} 删除", id, userId);
        statisticsService.evict();
    }

    /**
     * 点赞 / 取消点赞
     */
    @Transactional
    public LikeResult toggleLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }

        boolean alreadyLiked = commentLikeMapper.exists(commentId, userId);
        if (alreadyLiked) {
            commentLikeMapper.deleteByCommentAndUser(commentId, userId);
            commentMapper.decrementLikeCount(commentId);
        } else {
            commentLikeMapper.insert(commentId, userId);
            commentMapper.incrementLikeCount(commentId);
        }

        // 通知评论作者：点赞生成（或刷新）通知，取消点赞则撤回
        notificationService.notifyLikeComment(comment, userId, !alreadyLiked);

        return new LikeResult(!alreadyLiked, commentMapper.getLikeCount(commentId));
    }

    // ------------------------------------------------------------------
    // 统计
    // ------------------------------------------------------------------

    public int countAll() {
        return commentMapper.countAll();
    }

    public int countToday() {
        return commentMapper.countToday();
    }

    public int countByUser(Long userId) {
        return commentMapper.selectByUserId(userId).size();
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private void fillLiked(List<Comment> comments, Long currentUserId) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        if (currentUserId == null) {
            comments.forEach(comment -> comment.setLiked(false));
            return;
        }
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        Set<Long> likedIds = new HashSet<>(commentLikeMapper.selectLikedCommentIds(currentUserId, commentIds));
        comments.forEach(comment -> comment.setLiked(likedIds.contains(comment.getId())));
    }
}
