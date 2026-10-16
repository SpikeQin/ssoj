package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.ss.ssoj.common.BusinessException;
import com.ss.ssoj.common.JudgeStatus;
import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.config.JudgeProperties;
import com.ss.ssoj.dto.SubmissionQuery;
import com.ss.ssoj.dto.SubmissionRequest;
import com.ss.ssoj.entity.Problem;
import com.ss.ssoj.entity.Submission;
import com.ss.ssoj.entity.SysConfig;
import com.ss.ssoj.entity.SysUser;
import com.ss.ssoj.mapper.ProblemMapper;
import com.ss.ssoj.mapper.SubmissionMapper;
import com.ss.ssoj.mapper.SysConfigMapper;
import com.ss.ssoj.mapper.SysUserMapper;
import com.ss.ssoj.service.ContestService;
import com.ss.ssoj.service.JudgeService;
import com.ss.ssoj.service.SubmissionService;
import com.ss.ssoj.vo.CaseResultVO;
import com.ss.ssoj.vo.ContestVO;
import com.ss.ssoj.vo.SubmissionDetailVO;
import com.ss.ssoj.vo.SubmissionVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private static final int DEFAULT_CODE_LIMIT_KB = 32;
    private static final int DEFAULT_SUBMIT_INTERVAL_SECONDS = 10;

    private final SubmissionMapper submissionMapper;
    private final ProblemMapper problemMapper;
    private final SysUserMapper userMapper;
    private final SysConfigMapper configMapper;
    private final JudgeService judgeService;
    private final JudgeProperties judgeProperties;
    private final ContestService contestService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubmissionServiceImpl(SubmissionMapper submissionMapper, ProblemMapper problemMapper,
                                 SysUserMapper userMapper, SysConfigMapper configMapper,
                                 JudgeService judgeService, JudgeProperties judgeProperties,
                                 ContestService contestService) {
        this.submissionMapper = submissionMapper;
        this.problemMapper = problemMapper;
        this.userMapper = userMapper;
        this.configMapper = configMapper;
        this.judgeService = judgeService;
        this.judgeProperties = judgeProperties;
        this.contestService = contestService;
    }

    @Override
    public Long submit(SubmissionRequest request, Long userId) {
        Problem problem = problemMapper.selectById(request.getProblemId());
        if (problem == null || Integer.valueOf(0).equals(problem.getEnabled())) {
            throw new BusinessException(404, "题目不存在或已下线");
        }
        if (!judgeProperties.getLanguages().containsKey(request.getLanguage())) {
            throw new BusinessException("不支持的判题语言：" + request.getLanguage());
        }

        int codeLimit = readIntConfig("code_length_limit_kb", DEFAULT_CODE_LIMIT_KB) * 1024;
        if (request.getCode().getBytes(StandardCharsets.UTF_8).length > codeLimit) {
            throw new BusinessException("代码长度超过限制（" + codeLimit / 1024 + " KB）");
        }

        checkSubmitInterval(userId, request.getProblemId());

        if (judgeService.isQueueFull()) {
            throw new BusinessException("当前判题队列已满，请稍后再试");
        }

        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setProblemId(request.getProblemId());
        submission.setContestId(resolveContestId(request.getProblemId()));
        submission.setLanguage(request.getLanguage());
        submission.setCode(request.getCode());
        submission.setStatus(JudgeStatus.PENDING);
        submission.setSubmitTime(LocalDateTime.now());
        submissionMapper.insert(submission);

        problemMapper.update(null, new LambdaUpdateWrapper<Problem>()
                .eq(Problem::getId, problem.getId())
                .setSql("submit_count = submit_count + 1"));

        judgeService.submit(submission.getId());
        return submission.getId();
    }

    @Override
    public PageResult<SubmissionVO> page(SubmissionQuery query, Long currentUserId, boolean admin) {
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        if ("mine".equalsIgnoreCase(query.getScope())) {
            wrapper.eq(Submission::getUserId, currentUserId);
        } else if (admin && query.getUserId() != null) {
            wrapper.eq(Submission::getUserId, query.getUserId());
        }
        if (query.getProblemId() != null) {
            wrapper.eq(Submission::getProblemId, query.getProblemId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Submission::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getLanguage())) {
            wrapper.eq(Submission::getLanguage, query.getLanguage());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            List<SysUser> matched = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .select(SysUser::getId)
                    .and(w -> w.like(SysUser::getNickname, keyword).or().like(SysUser::getUsername, keyword)));
            if (matched.isEmpty()) {
                return new PageResult<>(new ArrayList<>(), 0);
            }
            wrapper.in(Submission::getUserId, matched.stream().map(SysUser::getId).toList());
        }
        wrapper.orderByDesc(Submission::getId);

        Page<Submission> page = submissionMapper.selectPage(
                new Page<>(Math.max(query.getPage(), 1), Math.max(query.getSize(), 1)), wrapper);
        return new PageResult<>(toVoList(page.getRecords()), page.getTotal());
    }

    @Override
    public SubmissionDetailVO detail(Long id, Long currentUserId, boolean admin) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw new BusinessException(404, "提交记录不存在");
        }
        if (!admin && !submission.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "只能查看自己的提交详情");
        }

        SubmissionDetailVO vo = new SubmissionDetailVO();
        fill(vo, submission);
        vo.setCode(submission.getCode());
        vo.setErrorMessage(submission.getErrorMessage());
        vo.setCases(parseCases(submission.getCaseDetail()));
        return vo;
    }

    /**
     * 判定本次提交归属哪场竞赛：以服务端时间为准，只看题目是否属于正在进行的竞赛，
     * 不采信前端传来的竞赛编号，避免伪造编号或赛后补交污染榜单。
     */
    private Long resolveContestId(Long problemId) {
        ContestVO running = contestService.runningContestsByProblem().get(problemId);
        return running == null ? null : running.getId();
    }

    private void checkSubmitInterval(Long userId, Long problemId) {
        int interval = readIntConfig("submit_interval_seconds", DEFAULT_SUBMIT_INTERVAL_SECONDS);
        if (interval <= 0) {
            return;
        }
        Submission last = submissionMapper.selectOne(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getUserId, userId)
                .eq(Submission::getProblemId, problemId)
                .orderByDesc(Submission::getId)
                .last("limit 1"));
        if (last == null || last.getSubmitTime() == null) {
            return;
        }
        long passed = Duration.between(last.getSubmitTime(), LocalDateTime.now()).getSeconds();
        if (passed < interval) {
            throw new BusinessException("提交过于频繁，请 " + (interval - passed) + " 秒后再试");
        }
    }

    private List<SubmissionVO> toVoList(List<Submission> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> problemIds = list.stream().map(Submission::getProblemId).collect(Collectors.toSet());
        Set<Long> userIds = list.stream().map(Submission::getUserId).collect(Collectors.toSet());

        Map<Long, String> titleMap = new HashMap<>();
        for (Problem problem : problemMapper.selectBatchIds(problemIds)) {
            titleMap.put(problem.getId(), problem.getTitle());
        }
        Map<Long, String> nicknameMap = new HashMap<>();
        for (SysUser user : userMapper.selectBatchIds(userIds)) {
            nicknameMap.put(user.getId(), user.getNickname());
        }

        List<SubmissionVO> result = new ArrayList<>();
        for (Submission submission : list) {
            SubmissionVO vo = new SubmissionVO();
            fill(vo, submission);
            vo.setProblemTitle(titleMap.getOrDefault(submission.getProblemId(), "已删除的题目"));
            vo.setNickname(nicknameMap.getOrDefault(submission.getUserId(), "未知用户"));
            result.add(vo);
        }
        return result;
    }

    private void fill(SubmissionVO vo, Submission submission) {
        vo.setId(submission.getId());
        vo.setUserId(submission.getUserId());
        vo.setProblemId(submission.getProblemId());
        vo.setContestId(submission.getContestId());
        vo.setLanguage(submission.getLanguage());
        vo.setStatus(submission.getStatus());
        vo.setTimeUsed(submission.getTimeUsed());
        vo.setCodeLength(submission.getCode() == null ? 0 : submission.getCode().length());
        vo.setSubmitTime(submission.getSubmitTime());
    }

    private List<CaseResultVO> parseCases(String caseDetail) {
        if (!StringUtils.hasText(caseDetail)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(caseDetail, new TypeReference<List<CaseResultVO>>() {
            });
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private int readIntConfig(String key, int defaultValue) {
        SysConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
