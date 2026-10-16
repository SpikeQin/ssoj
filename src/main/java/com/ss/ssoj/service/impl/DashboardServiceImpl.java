package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ss.ssoj.common.JudgeStatus;
import com.ss.ssoj.dto.SubmissionQuery;
import com.ss.ssoj.entity.Problem;
import com.ss.ssoj.entity.Submission;
import com.ss.ssoj.entity.SysUser;
import com.ss.ssoj.mapper.ProblemMapper;
import com.ss.ssoj.mapper.SubmissionMapper;
import com.ss.ssoj.mapper.SysUserMapper;
import com.ss.ssoj.service.DashboardService;
import com.ss.ssoj.service.SubmissionService;
import com.ss.ssoj.vo.DashboardVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final SysUserMapper userMapper;
    private final ProblemMapper problemMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionService submissionService;

    public DashboardServiceImpl(SysUserMapper userMapper, ProblemMapper problemMapper,
                                SubmissionMapper submissionMapper, SubmissionService submissionService) {
        this.userMapper = userMapper;
        this.problemMapper = problemMapper;
        this.submissionMapper = submissionMapper;
        this.submissionService = submissionService;
    }

    @Override
    public DashboardVO summary() {
        DashboardVO vo = new DashboardVO();
        vo.setUserCount(Math.toIntExact(userMapper.selectCount(new LambdaQueryWrapper<SysUser>())));
        vo.setProblemCount(Math.toIntExact(problemMapper.selectCount(new LambdaQueryWrapper<Problem>())));
        vo.setSubmissionCount(Math.toIntExact(submissionMapper.selectCount(new LambdaQueryWrapper<Submission>())));
        vo.setPendingCount(Math.toIntExact(submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .in(Submission::getStatus, JudgeStatus.PENDING, JudgeStatus.JUDGING))));
        vo.setTodaySubmission(Math.toIntExact(submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .ge(Submission::getSubmitTime, LocalDate.now().atStartOfDay()))));

        Long accepted = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStatus, JudgeStatus.ACCEPTED));
        vo.setPassRate(vo.getSubmissionCount() == 0
                ? 0 : Math.round(accepted * 100f / vo.getSubmissionCount()));

        SubmissionQuery query = new SubmissionQuery();
        query.setScope("all");
        query.setSize(6);
        vo.setRecent(submissionService.page(query, null, true).getRecords());
        return vo;
    }
}
