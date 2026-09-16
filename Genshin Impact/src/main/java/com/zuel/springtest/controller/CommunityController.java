package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.services.CommunityService;
import com.zuel.springtest.vo.CommunityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区接口
 */
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    /** 社区列表 */
    @GetMapping
    public Result<List<CommunityVO>> list() {
        return Result.success(communityService.listCommunities());
    }

    /** 单个社区 */
    @GetMapping("/{id}")
    public Result<CommunityVO> get(@PathVariable Integer id) {
        return Result.success(communityService.getCommunity(id));
    }
}
