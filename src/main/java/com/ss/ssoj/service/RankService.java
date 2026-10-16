package com.ss.ssoj.service;

import com.ss.ssoj.vo.PersonalStatsVO;
import com.ss.ssoj.vo.RankRowVO;

import java.util.List;

public interface RankService {

    /** 总榜：按通过题数与提交次数排序 */
    List<RankRowVO> globalRank(int limit);

    /** 个人统计与最近提交 */
    PersonalStatsVO personalStats(Long userId);
}
