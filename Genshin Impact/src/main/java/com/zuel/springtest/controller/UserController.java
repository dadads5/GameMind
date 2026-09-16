package com.zuel.springtest.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zuel.springtest.common.PageResult;
import com.zuel.springtest.common.Result;
import com.zuel.springtest.dto.post.PostQuery;
import com.zuel.springtest.entity.User;
import com.zuel.springtest.mapper.UserLikeMapper;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.CommentService;
import com.zuel.springtest.services.PostService;
import com.zuel.springtest.services.UserService;
import com.zuel.springtest.vo.CommentVO;
import com.zuel.springtest.vo.PostVO;
import com.zuel.springtest.vo.UserStatsVO;
import com.zuel.springtest.vo.UserVO;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口
 *
 * <p>注册、登录、资料维护统一走 {@code /api/auth}；当前用户的资源集中在 {@code /api/users/me/**}。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final UserLikeMapper userLikeMapper;

    /** 用户公开资料（含主页点赞数与当前登录用户今日是否已赞） */
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id,
                                  @CurrentUser(required = false) LoginUser loginUser) {
        User user = userService.getById(id);
        UserVO vo = UserVO.from(user);
        if (loginUser != null) {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            vo.setTodayLiked(userLikeMapper.countByLikerAndDate(loginUser.getId(), id, today) > 0);
        }
        return Result.success(vo);
    }

    /** 用户活跃度统计（发帖/回复/获赞/主页获赞，公开） */
    @GetMapping("/{id}/stats")
    public Result<UserStatsVO> userStats(@PathVariable Long id) {
        return Result.success(userService.getStats(id));
    }

    /** 给某用户主页点赞（每天一次，需登录） */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> likeUser(@PathVariable Long id,
                                                 @CurrentUser LoginUser loginUser) {
        userService.likeUser(loginUser.getId(), id);
        int likeCount = userLikeMapper.countByLikedUser(id);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("likeCount", likeCount);
        data.put("todayLiked", true);
        return Result.success(data);
    }

    /** 某用户发布的帖子（公开） */
    @GetMapping("/{id}/posts")
    public Result<PageResult<PostVO>> userPosts(@PathVariable Long id,
                                                @ModelAttribute PostQuery query) {
        query.setAuthorId(id);
        return Result.success(postService.listPosts(query, id));
    }

    /** 用户列表（分页） */
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }
        PageHelper.startPage(page, size);
        PageInfo<User> pageInfo = new PageInfo<>(userService.getAllUsers());
        return Result.success(PageResult.of(pageInfo).map(UserVO::from));
    }

    /** 我发布的帖子 */
    @GetMapping("/me/posts")
    public Result<PageResult<PostVO>> myPosts(@ModelAttribute PostQuery query,
                                              @CurrentUser LoginUser loginUser) {
        query.setAuthorId(loginUser.getId());
        return Result.success(postService.listPosts(query, loginUser.getId()));
    }

    /** 我发表的评论 */
    @GetMapping("/me/comments")
    public Result<PageResult<CommentVO>> myComments(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @CurrentUser LoginUser loginUser) {
        return Result.success(
                commentService.listByUser(loginUser.getId(), page, size, loginUser.getId()));
    }
}
