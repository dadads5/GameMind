package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.entity.Board;
import com.zuel.springtest.entity.Community;
import com.zuel.springtest.security.CurrentUser;
import com.zuel.springtest.security.LoginUser;
import com.zuel.springtest.services.AdminService;
import com.zuel.springtest.services.CommunityService;
import com.zuel.springtest.vo.CommunityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理后台接口（需登录且角色为管理员）
 *
 * <p>前端 baseURL 为 /api，实际路径 /api/admin/**。
 * 所有接口统一先做管理员校验，前端隐藏入口只是辅助，安全以服务端校验为准。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CommunityService communityService;

    // ------------------------------------------------------------------
    // 数据看板
    // ------------------------------------------------------------------

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        return Result.success(adminService.dashboard());
    }

    // ------------------------------------------------------------------
    // 内容：帖子
    // ------------------------------------------------------------------

    /** 帖子列表（跨社区，支持关键词搜索与板块筛选） */
    @GetMapping("/posts")
    public Result<Map<String, Object>> listPosts(@CurrentUser LoginUser loginUser,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer boardId,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        adminService.assertAdmin(loginUser);
        return Result.success(adminService.listPosts(keyword, boardId, page, size));
    }

    @PutMapping("/posts/{id}/top")
    public Result<Boolean> toggleTop(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        boolean top = adminService.toggleTop(id);
        return Result.success(top ? "已置顶" : "已取消置顶", top);
    }

    @PutMapping("/posts/{id}/essence")
    public Result<Boolean> toggleEssence(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        boolean essence = adminService.toggleEssence(id);
        return Result.success(essence ? "已设为精华" : "已取消精华", essence);
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.deletePost(id);
        return Result.success("已删除", null);
    }

    // ------------------------------------------------------------------
    // 内容：评论
    // ------------------------------------------------------------------

    @GetMapping("/comments")
    public Result<Map<String, Object>> listComments(@CurrentUser LoginUser loginUser,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        adminService.assertAdmin(loginUser);
        return Result.success(adminService.listComments(keyword, page, size));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.deleteComment(id);
        return Result.success("已删除", null);
    }

    // ------------------------------------------------------------------
    // 用户
    // ------------------------------------------------------------------

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(@CurrentUser LoginUser loginUser,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        adminService.assertAdmin(loginUser);
        return Result.success(adminService.listUsers(keyword, page, size));
    }

    /** 启停账号：status=1 正常，0 禁用 */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @RequestParam int status,
                                         @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.updateUserStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用", null);
    }

    /** 设置角色：role=0 普通用户，1 管理员 */
    @PutMapping("/users/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id,
                                       @RequestParam int role,
                                       @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.updateUserRole(id, role);
        return Result.success(role == 1 ? "已设为管理员" : "已取消管理员", null);
    }

    /** 设置 VIP：vip=0 否，1 是 */
    @PutMapping("/users/{id}/vip")
    public Result<Void> updateUserVip(@PathVariable Long id,
                                       @RequestParam int vip,
                                       @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.updateUserVip(id, vip);
        return Result.success(vip == 1 ? "已设为 VIP" : "已取消 VIP", null);
    }

    /** 重置用户密码：管理员直接设置新密码，无需提供用户原密码 */
    @PutMapping("/users/{id}/password")
    public Result<Void> updateUserPassword(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.updateUserPassword(id, body == null ? null : body.get("password"));
        return Result.success("密码已修改", null);
    }

    // ------------------------------------------------------------------
    // 板块
    // ------------------------------------------------------------------

    @GetMapping("/boards")
    public Result<List<Board>> listBoards(@CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        return Result.success(adminService.listBoards());
    }

    @PostMapping("/boards")
    public Result<Board> createBoard(@RequestBody Board board, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        return Result.success("板块已创建", adminService.createBoard(board));
    }

    @PutMapping("/boards/{id}")
    public Result<Void> updateBoard(@PathVariable Integer id,
                                    @RequestBody Board board,
                                    @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        board.setId(id);
        adminService.updateBoard(board);
        return Result.success("已保存", null);
    }

    @DeleteMapping("/boards/{id}")
    public Result<Void> deleteBoard(@PathVariable Integer id, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        adminService.deleteBoard(id);
        return Result.success("已删除", null);
    }

    // ------------------------------------------------------------------
    // 游戏社区（论坛型社区，管理员可增删改）
    // ------------------------------------------------------------------

    @PostMapping("/communities")
    public Result<CommunityVO> createCommunity(@RequestBody Community community, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        return Result.success("社区已创建", communityService.createCommunity(community));
    }

    @PutMapping("/communities/{id}")
    public Result<Void> updateCommunity(@PathVariable Integer id,
                                        @RequestBody Community community,
                                        @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        communityService.updateCommunity(id, community);
        return Result.success("已保存", null);
    }

    @DeleteMapping("/communities/{id}")
    public Result<Void> deleteCommunity(@PathVariable Integer id, @CurrentUser LoginUser loginUser) {
        adminService.assertAdmin(loginUser);
        communityService.deleteCommunity(id);
        return Result.success("已删除", null);
    }
}
