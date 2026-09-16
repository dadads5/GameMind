package com.zuel.springtest.controller;

import com.zuel.springtest.common.Result;
import com.zuel.springtest.services.BoardService;
import com.zuel.springtest.vo.BoardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 板块接口
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /** 全部板块 */
    @GetMapping
    public Result<List<BoardVO>> listBoards() {
        return Result.success(boardService.listBoards());
    }

    /** 指定社区下的板块（如 communityId=2 为火影忍者） */
    @GetMapping("/community/{communityId}")
    public Result<List<BoardVO>> listByCommunity(@PathVariable Integer communityId) {
        return Result.success(boardService.listBoardsByCommunity(communityId));
    }

    /** 单个板块 */
    @GetMapping("/{id}")
    public Result<BoardVO> getBoard(@PathVariable Integer id) {
        return Result.success(boardService.getBoard(id));
    }
}
