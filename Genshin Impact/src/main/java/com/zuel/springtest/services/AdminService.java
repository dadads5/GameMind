package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.Board;
import com.zuel.springtest.entity.Comment;
import com.zuel.springtest.entity.Post;
import com.zuel.springtest.mapper.BoardMapper;
import com.zuel.springtest.mapper.CommentMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.mapper.UserMapper;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员业务
 *
 * <p>覆盖：数据看板、内容（帖子/评论）管理、用户管理（启停/角色/VIP/重置密码）、板块管理。
 * 所有入口都要求当前用户 role = 1（管理员）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    /** 管理员角色值 */
    public static final int ROLE_ADMIN = 1;
    /** 普通用户角色值 */
    public static final int ROLE_USER = 0;

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final BoardMapper boardMapper;
    private final PostService postService;
    private final PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------
    // 权限
    // ------------------------------------------------------------------

    /** 校验管理员身份，非管理员抛 403 */
    public void assertAdmin(LoginUser loginUser) {
        if (loginUser == null || !Integer.valueOf(ROLE_ADMIN).equals(loginUser.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    // ------------------------------------------------------------------
    // 数据看板
    // ------------------------------------------------------------------

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userTotal", userMapper.countAll());
        data.put("userToday", userMapper.countToday());
        data.put("postTotal", postMapper.countAll());
        data.put("postToday", postMapper.countToday());
        data.put("commentTotal", commentMapper.countAll());
        data.put("commentToday", commentMapper.countToday());
        data.put("boardTotal", boardMapper.countAll());
        data.put("viewTotal", postMapper.sumViewCount());
        return data;
    }

    // ------------------------------------------------------------------
    // 内容：帖子
    // ------------------------------------------------------------------

    public Map<String, Object> listPosts(String keyword, Integer boardId, int page, int size) {
        String kw = normalize(keyword);
        int[] range = pageRange(page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", postMapper.selectForAdmin(kw, boardId, range[1], range[0]));
        data.put("total", postMapper.countForAdmin(kw, boardId));
        return data;
    }

    /** 切换置顶，返回切换后的状态 */
    public boolean toggleTop(Long postId) {
        Post post = requirePost(postId);
        boolean next = !Boolean.TRUE.equals(post.getIsTop());
        postMapper.updateTop(postId, next ? 1 : 0);
        return next;
    }

    /** 切换精华，返回切换后的状态 */
    public boolean toggleEssence(Long postId) {
        Post post = requirePost(postId);
        boolean next = !Boolean.TRUE.equals(post.getIsEssence());
        postMapper.updateEssence(postId, next ? 1 : 0);
        return next;
    }

    /**
     * 管理员删除帖子（物理删除）
     *
     * <p>级联清理委托给 PostService，与作者自删保持同一套逻辑，
     * 避免遗漏 post_like / post_image / comment_like 造成孤儿数据。
     */
    @Transactional
    public void deletePost(Long postId) {
        requirePost(postId);
        postService.deletePostByAdmin(postId);
    }

    // ------------------------------------------------------------------
    // 内容：评论
    // ------------------------------------------------------------------

    public Map<String, Object> listComments(String keyword, int page, int size) {
        String kw = normalize(keyword);
        int[] range = pageRange(page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", commentMapper.selectForAdmin(kw, range[1], range[0]));
        data.put("total", commentMapper.countForAdmin(kw));
        return data;
    }

    /** 删除评论并重算所属帖子的评论数 */
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评论不存在");
        }
        commentMapper.deleteById(commentId);
        postMapper.refreshCommentCount(comment.getPostId());
    }

    // ------------------------------------------------------------------
    // 用户
    // ------------------------------------------------------------------

    public Map<String, Object> listUsers(String keyword, int page, int size) {
        String kw = normalize(keyword);
        int[] range = pageRange(page, size);
        List<AdminUserVO> list = userMapper.selectForAdmin(kw, range[1], range[0])
                .stream()
                .map(AdminUserVO::from)
                .toList();
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", userMapper.countForAdmin(kw));
        return data;
    }

    /** 启停账号：1 正常，0 禁用 */
    public void updateUserStatus(Long userId, int status) {
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态值非法");
        }
        requireUser(userId);
        userMapper.updateStatus(userId, status);
        log.info("管理员修改用户 {} 状态为 {}", userId, status);
    }

    /** 设置角色：0 普通用户，1 管理员 */
    public void updateUserRole(Long userId, int role) {
        if (role != ROLE_USER && role != ROLE_ADMIN) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色值非法");
        }
        requireUser(userId);
        userMapper.updateRole(userId, role);
        log.info("管理员修改用户 {} 角色为 {}", userId, role);
    }

    /** 设置 VIP：0 否，1 是 */
    public void updateUserVip(Long userId, int vip) {
        if (vip != 0 && vip != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "VIP 值非法");
        }
        requireUser(userId);
        userMapper.updateVip(userId, vip);
        log.info("管理员修改用户 {} VIP 为 {}", userId, vip);
    }

    /**
     * 重置用户密码（管理员操作，无需提供用户原密码）。
     *
     * <p>入参为明文新密码，长度 6-100 位，入库前统一用 BCrypt 加密，
     * 与注册/登录保持一致，绝不落明文。
     */
    @Transactional
    public void updateUserPassword(Long userId, String rawPassword) {
        if (!StringUtils.hasText(rawPassword) || rawPassword.length() < 6 || rawPassword.length() > 100) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "密码长度需要 6-100 位");
        }
        requireUser(userId);
        userMapper.updatePassword(userId, passwordEncoder.encode(rawPassword));
        log.info("管理员重置用户 {} 的密码", userId);
    }

    // ------------------------------------------------------------------
    // 板块
    // ------------------------------------------------------------------

    public List<Board> listBoards() {
        return boardMapper.selectAll();
    }

    public Board createBoard(Board board) {
        validateBoard(board);
        board.setId(boardMapper.selectMaxId() + 1);
        if (boardMapper.insert(board) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建板块失败");
        }
        return board;
    }

    public void updateBoard(Board board) {
        requireBoard(board.getId());
        validateBoard(board);
        boardMapper.update(board);
    }

    /** 删除板块：板块下仍有帖子时拒绝，避免产生无主帖子 */
    public void deleteBoard(Integer boardId) {
        requireBoard(boardId);
        int postCount = postMapper.countByBoardIds(List.of(boardId));
        if (postCount > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该板块下还有 " + postCount + " 篇帖子，请先处理");
        }
        boardMapper.deleteById(boardId);
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private Post requirePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private void requireUser(Long id) {
        if (userMapper.selectById(id).isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
    }

    private void requireBoard(Integer id) {
        if (id == null || boardMapper.selectById(id).isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }
    }

    private void validateBoard(Board board) {
        if (board == null || board.getCommunityId() == null || !StringUtils.hasText(board.getName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "板块名称与所属社区不能为空");
        }
    }

    private String normalize(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    /** 返回 [offset, size] */
    private int[] pageRange(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return new int[]{(safePage - 1) * safeSize, safeSize};
    }
}
