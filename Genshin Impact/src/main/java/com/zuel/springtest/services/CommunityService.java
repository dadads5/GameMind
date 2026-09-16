package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.Board;
import com.zuel.springtest.entity.Community;
import com.zuel.springtest.mapper.BoardMapper;
import com.zuel.springtest.mapper.CommunityMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.vo.CommunityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 社区业务
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;
    private final BoardMapper boardMapper;
    private final PostMapper postMapper;

    public List<CommunityVO> listCommunities() {
        return communityMapper.selectAll().stream().map(CommunityVO::from).toList();
    }

    public CommunityVO getCommunity(Integer id) {
        Community community = communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社区不存在");
        }
        return CommunityVO.from(community);
    }

    /**
     * 新增游戏社区（论坛型）：自动分配安全 id（>=5，避开原 4 个内置社区），
     * 并创建一个默认板块「综合讨论」，保证新建社区可立即发帖。
     */
    @Transactional
    public CommunityVO createCommunity(Community community) {
        if (community == null || !hasText(community.getName())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "社区名称不能为空");
        }
        int maxId = communityMapper.selectMaxId();
        int nextId = Math.max(maxId + 1, 5);
        community.setId(nextId);
        communityMapper.insert(community);

        Board board = new Board();
        board.setId(boardMapper.selectMaxId() + 1);
        board.setCommunityId(nextId);
        board.setName("综合讨论");
        board.setIcon(community.getIcon());
        board.setDescription("综合讨论区");
        board.setSort(0);
        boardMapper.insert(board);

        return CommunityVO.from(community);
    }

    @Transactional
    public CommunityVO updateCommunity(Integer id, Community community) {
        Community existing = communityMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社区不存在");
        }
        existing.setName(community.getName());
        existing.setIcon(community.getIcon());
        existing.setDescription(community.getDescription());
        existing.setSort(community.getSort());
        communityMapper.update(existing);
        return CommunityVO.from(existing);
    }

    @Transactional
    public void deleteCommunity(Integer id) {
        Community existing = communityMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社区不存在");
        }
        List<Integer> boardIds = boardMapper.selectByCommunityId(id).stream().map(Board::getId).toList();
        int postCount = postMapper.countByBoardIds(boardIds);
        if (postCount > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该社区下还有 " + postCount + " 篇帖子，请先处理");
        }
        boardMapper.deleteByCommunityId(id);
        communityMapper.deleteById(id);
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
