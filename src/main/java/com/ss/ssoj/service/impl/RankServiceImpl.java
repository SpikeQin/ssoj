package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ss.ssoj.common.JudgeStatus;
import com.ss.ssoj.dto.SubmissionQuery;
import com.ss.ssoj.entity.Submission;
import com.ss.ssoj.mapper.StatsMapper;
import com.ss.ssoj.mapper.SubmissionMapper;
import com.ss.ssoj.service.RankService;
import com.ss.ssoj.service.SubmissionService;
import com.ss.ssoj.vo.PersonalStatsVO;
import com.ss.ssoj.vo.RankRowVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankServiceImpl implements RankService {

    private static final Map<Integer, String> STATUS_NAME = Map.of(
            JudgeStatus.PENDING, "pending",
            JudgeStatus.JUDGING, "judging",
            JudgeStatus.ACCEPTED, "ac",
            JudgeStatus.WRONG_ANSWER, "wa",
            JudgeStatus.TIME_LIMIT_EXCEEDED, "tle",
            JudgeStatus.RUNTIME_ERROR, "re",
            JudgeStatus.COMPILE_ERROR, "ce",
            JudgeStatus.SYSTEM_ERROR, "se");

    private final StatsMapper statsMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionService submissionService;

    public RankServiceImpl(StatsMapper statsMapper, SubmissionMapper submissionMapper,
                           SubmissionService submissionService) {
        this.statsMapper = statsMapper;
        this.submissionMapper = submissionMapper;
        this.submissionService = submissionService;
    }

    @Override
    public List<RankRowVO> globalRank(int limit) {
        List<RankRowVO> rows = statsMapper.selectGlobalRank(limit <= 0 ? 100 : limit);
        for (int index = 0; index < rows.size(); index++) {
            RankRowVO row = rows.get(index);
            row.setRank(index + 1);
            row.setAcceptRate(rate(row.getAcceptCount(), row.getSubmitCount()));
        }
        return rows;
    }

    @Override
    public PersonalStatsVO personalStats(Long userId) {
        RankRowVO stat = statsMapper.selectUserStat(userId);
        PersonalStatsVO vo = new PersonalStatsVO();
        vo.setSubmitCount(stat == null || stat.getSubmitCount() == null ? 0 : stat.getSubmitCount());
        vo.setAcCount(stat == null || stat.getSolved() == null ? 0 : stat.getSolved());
        vo.setAcceptRate(rate(stat == null ? 0 : stat.getAcceptCount(), vo.getSubmitCount()));

        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        List<Submission> mine = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getUserId, userId)
                .select(Submission::getStatus));
        for (Submission submission : mine) {
            String name = STATUS_NAME.getOrDefault(submission.getStatus(), "unknown");
            statusCounts.merge(name, 1, Integer::sum);
        }
        vo.setStatusCounts(statusCounts);

        SubmissionQuery query = new SubmissionQuery();
        query.setScope("mine");
        query.setSize(5);
        vo.setRecent(submissionService.page(query, userId, false).getRecords());
        return vo;
    }

    private int rate(Integer acceptCount, Integer submitCount) {
        if (submitCount == null || submitCount == 0 || acceptCount == null) {
            return 0;
        }
        return Math.round(acceptCount * 100f / submitCount);
    }
}
