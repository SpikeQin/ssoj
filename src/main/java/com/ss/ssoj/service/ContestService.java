package com.ss.ssoj.service;

import com.ss.ssoj.dto.ContestSaveRequest;
import com.ss.ssoj.vo.ContestRankVO;
import com.ss.ssoj.vo.ContestVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContestService {

    /** 竞赛列表，按开始时间倒序 */
    List<ContestVO> list();

    /** 竞赛详情，包含题目清单 */
    Map<String, Object> detail(Long id);

    /** 竞赛榜单：按通过题数与罚时排名 */
    ContestRankVO rank(Long id);

    /**
     * 当前正在进行（开始时间已到、结束时间未过）的竞赛所包含的题目。
     * 键为题目编号，值为该题目所在的竞赛；同一道题同时出现在多场进行中的竞赛时，
     * 取开始时间较晚的一场，用于判定提交归属哪场竞赛。
     */
    Map<Long, ContestVO> runningContestsByProblem();

    /**
     * 未结束（开始时间已到或尚未开始）且开启了“隐藏赛题”的竞赛所包含的题目编号，
     * 这些题目不会出现在学生的题库列表与检索结果中，只能从竞赛页面进入。
     */
    Set<Long> hiddenProblemIds();

    /** 创建竞赛并编排题目 */
    Long save(ContestSaveRequest request, Long operatorId);

    /** 修改竞赛信息与题目编排 */
    Long update(Long id, ContestSaveRequest request, Long operatorId);

    /** 删除竞赛（逻辑删除），同时解除题目编排 */
    void delete(Long id);
}
