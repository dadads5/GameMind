package com.zuel.springtest.controller;

import com.zuel.springtest.common.PageResult;
import com.zuel.springtest.common.Result;
import com.zuel.springtest.dto.post.PostCreateRequest;
import com.zuel.springtest.dto.post.PostQuery;
import com.zuel.springtest.dto.post.PostUpdateRequest;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.PostService;
import com.zuel.springtest.vo.LikeResult;
import com.zuel.springtest.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 帖子接口
 *
 * <p>发帖、编辑、删除、点赞均从 {@code @CurrentUser} 取身份，不再信任请求体中的 userId。
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 帖子列表：支持板块筛选、关键词搜索、排序与分页
     *
     * <p>示例：{@code GET /api/posts?boardId=7&keyword=螺旋丸&sort=hot&page=1&size=10}
     */
    @GetMapping
    public Result<PageResult<PostVO>> list(@ModelAttribute PostQuery query,
                                           @CurrentUser(required = false) LoginUser loginUser) {
        return Result.success(postService.listPosts(query, currentUserId(loginUser)));
    }

    /** 帖子详情，浏览量 +1 */
    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id,
                                 @CurrentUser(required = false) LoginUser loginUser) {
        return Result.success(postService.getPostDetail(id, currentUserId(loginUser)));
    }

    /**
     * 按板块查询（兼容旧版 /api/posts/game/{id} 路径）
     */
    @GetMapping("/game/{boardId}")
    public Result<PageResult<PostVO>> listByBoard(@PathVariable Integer boardId,
                                                  @ModelAttribute PostQuery query,
                                                  @CurrentUser(required = false) LoginUser loginUser) {
        query.setBoardId(boardId);
        return Result.success(postService.listPosts(query, currentUserId(loginUser)));
    }

    /** 热门帖子 */
    @GetMapping("/hot")
    public Result<List<PostVO>> hotPosts(@RequestParam(required = false) Integer boardId,
                                         @RequestParam(required = false) Integer communityId,
                                         @RequestParam(defaultValue = "10") int limit,
                                         @CurrentUser(required = false) LoginUser loginUser) {
        return Result.success(postService.listHotPosts(boardId, communityId, limit, currentUserId(loginUser)));
    }

    /** 发帖 */
    @PostMapping
    public Result<PostVO> create(@CurrentUser LoginUser loginUser,
                                 @Valid @RequestBody PostCreateRequest request) {
        return Result.success("发布成功", postService.createPost(loginUser.getId(), request));
    }

    /** 编辑帖子 */
    @PutMapping("/{id}")
    public Result<PostVO> update(@PathVariable Long id,
                                 @CurrentUser LoginUser loginUser,
                                 @Valid @RequestBody PostUpdateRequest request) {
        return Result.success("已保存", postService.updatePost(id, loginUser.getId(), request));
    }

    /** 删除帖子 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        postService.deletePost(id, loginUser.getId());
        return Result.success("删除成功", null);
    }

    /** 点赞 / 取消点赞 */
    @PostMapping("/{id}/like")
    public Result<LikeResult> toggleLike(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        LikeResult result = postService.toggleLike(id, loginUser.getId());
        return Result.success(result.liked() ? "点赞成功" : "已取消点赞", result);
    }

    private Long currentUserId(LoginUser loginUser) {
        return loginUser == null ? null : loginUser.getId();
    }
}
