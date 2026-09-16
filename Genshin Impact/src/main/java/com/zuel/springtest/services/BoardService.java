package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.Board;
import com.zuel.springtest.mapper.BoardMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.vo.BoardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 板块业务
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;
    private final PostMapper postMapper;

    public List<BoardVO> listBoards() {
        List<BoardVO> vos = boardMapper.selectAll().stream()
                .map(BoardVO::from)
                .toList();
        fillTodayPostCount(vos);
        return vos;
    }

    public List<BoardVO> listBoardsByCommunity(Integer communityId) {
        List<BoardVO> vos = boardMapper.selectByCommunityId(communityId).stream()
                .map(BoardVO::from)
                .toList();
        fillTodayPostCount(vos);
        return vos;
    }

    public BoardVO getBoard(Integer id) {
        Board board = boardMapper.selectById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "板块不存在"));
        BoardVO vo = BoardVO.from(board);
        fillTodayPostCount(vo);
        return vo;
    }

    public int countAll() {
        return boardMapper.countAll();
    }

    private void fillTodayPostCount(BoardVO vo) {
        if (vo != null) {
            vo.setTodayPostCount(postMapper.countTodayByBoardId(vo.getId()));
        }
    }

    private void fillTodayPostCount(List<BoardVO> vos) {
        if (vos == null || vos.isEmpty()) {
            return;
        }
        List<Integer> boardIds = vos.stream()
                .map(BoardVO::getId)
                .filter(Objects::nonNull)
                .toList();
        if (boardIds.isEmpty()) {
            return;
        }
        Map<Integer, Integer> countMap = postMapper.countTodayByBoardIds(boardIds).stream()
                .collect(Collectors.toMap(
                        m -> (Integer) m.get("board_id"),
                        m -> ((Number) m.get("cnt")).intValue(),
                        (a, b) -> a));
        vos.forEach(vo -> vo.setTodayPostCount(countMap.getOrDefault(vo.getId(), 0)));
    }
}
