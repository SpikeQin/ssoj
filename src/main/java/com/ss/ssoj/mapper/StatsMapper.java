package com.ss.ssoj.mapper;

import com.ss.ssoj.vo.ContestStatVO;
import com.ss.ssoj.vo.RankRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计类查询使用自定义 SQL，语句写在 resources/mapper/StatsMapper.xml 中。
 */
@Mapper
public interface StatsMapper {

    /** 总榜：按通过题数排序，题数相同时提交次数少的靠前 */
    List<RankRowVO> selectGlobalRank(@Param("limit") int limit);

    /** 各竞赛的参与人数 */
    List<ContestStatVO> selectContestParticipants();

    /** 指定用户的提交统计 */
    RankRowVO selectUserStat(@Param("userId") Long userId);

    /** 全部用户的提交与通过数量，用于后台用户列表 */
    List<RankRowVO> selectUserStats();
}
