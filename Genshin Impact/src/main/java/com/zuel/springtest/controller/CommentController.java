package com.zuel.springtest.controller;

import com.zuel.springtest.common.PageResult;
import com.zuel.springtest.common.Result;
import com.zuel.springtest.dto.comment.CommentCreateRequest;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.CommentService;
import com.zuel.springtest.vo.CommentVO;
import com.zuel.springtest.vo.LikeResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 帖子下的评论列表（分页，默认第 1 页 20 条，单页最多 100 条）
     */
    @GetMapping("/post/{postId}")
    public Result<PageResult<CommentVO>> listByPost(@PathVariable Long postId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @CurrentUser(required = false) LoginUser loginUser) {
        return Result.success(commentService.listByPost(postId, currentUserId(loginUser), page, size));
    }

    /**
     * 发表评论
     */
    @PostMapping("/post/{postId}")
    public Result<CommentVO> create(@PathVariable Long postId,
                                    @CurrentUser LoginUser loginUser,
                                    @Valid @RequestBody CommentCreateRequest request) {
        return Result.success("评论已发布",
                commentService.create(postId, loginUser.getId(), request.getContent()));
    }

    /**
     * 删除评论（评论作者或帖子作者可删）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        commentService.delete(id, loginUser.getId());
        return Result.success("删除成功", null);
    }

    /**
     * 点赞 / 取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<LikeResult> toggleLike(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        LikeResult result = commentService.toggleLike(id, loginUser.getId());
        return Result.success(result.liked() ? "点赞成功" : "已取消点赞", result);
    }

    private Long currentUserId(LoginUser loginUser) {
        return loginUser == null ? null : loginUser.getId();
    }
}
