package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ss.ssoj.common.BusinessException;
import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.dto.ProblemCaseRequest;
import com.ss.ssoj.dto.ProblemQuery;
import com.ss.ssoj.dto.ProblemSaveRequest;
import com.ss.ssoj.entity.Problem;
import com.ss.ssoj.entity.ProblemCase;
import com.ss.ssoj.entity.Submission;
import com.ss.ssoj.mapper.ProblemCaseMapper;
import com.ss.ssoj.mapper.ProblemMapper;
import com.ss.ssoj.mapper.SubmissionMapper;
import com.ss.ssoj.service.ContestService;
import com.ss.ssoj.service.ProblemService;
import com.ss.ssoj.vo.ContestVO;
import com.ss.ssoj.vo.ProblemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProblemServiceImpl implements ProblemService {

    /** 判题状态：通过 */
    private static final int STATUS_ACCEPTED = 2;

    private final ProblemMapper problemMapper;
    private final ProblemCaseMapper caseMapper;
    private final SubmissionMapper submissionMapper;
    private final ContestService contestService;

    public ProblemServiceImpl(ProblemMapper problemMapper, ProblemCaseMapper caseMapper,
                              SubmissionMapper submissionMapper, ContestService contestService) {
        this.problemMapper = problemMapper;
        this.caseMapper = caseMapper;
        this.submissionMapper = submissionMapper;
        this.contestService = contestService;
    }

    @Override
    public PageResult<ProblemVO> page(ProblemQuery query, Long userId, boolean admin) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        if (!admin) {
            // 学生只能看到已上线的题目；未结束竞赛的赛题从题库中隐藏，只能从竞赛页面进入
            wrapper.eq(Problem::getEnabled, 1);
            Set<Long> hidden = contestService.hiddenProblemIds();
            if (!hidden.isEmpty()) {
                wrapper.notIn(Problem::getId, hidden);
            }
        }

        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> {
                w.like(Problem::getTitle, keyword);
                if (keyword.matches("\\d+")) {
                    w.or().eq(Problem::getId, Long.valueOf(keyword));
                }
            });
        }
        if (query.getDifficulty() != null) {
            wrapper.eq(Problem::getDifficulty, query.getDifficulty());
        }

        Set<Long> accepted = new HashSet<>();
        Set<Long> attempted = new HashSet<>();
        if (userId != null) {
            List<Submission> mine = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getUserId, userId)
                    .select(Submission::getProblemId, Submission::getStatus));
            for (Submission item : mine) {
                attempted.add(item.getProblemId());
                if (Integer.valueOf(STATUS_ACCEPTED).equals(item.getStatus())) {
                    accepted.add(item.getProblemId());
                }
            }
        }

        if (StringUtils.hasText(query.getStatus())) {
            switch (query.getStatus()) {
                case "ac" -> {
                    if (accepted.isEmpty()) {
                        return new PageResult<>(List.of(), 0);
                    }
                    wrapper.in(Problem::getId, accepted);
                }
                case "wa" -> {
                    Set<Long> wrong = new HashSet<>(attempted);
                    wrong.removeAll(accepted);
                    if (wrong.isEmpty()) {
                        return new PageResult<>(List.of(), 0);
                    }
                    wrapper.in(Problem::getId, wrong);
                }
                case "none" -> {
                    if (!attempted.isEmpty()) {
                        wrapper.notIn(Problem::getId, attempted);
                    }
                }
                default -> {
                    // 非法状态值按不过滤处理
                }
            }
        }

        // 学生端按题号顺序浏览，管理端把最新创建的题目排在前面
        if (admin) {
            wrapper.orderByDesc(Problem::getId);
        } else {
            wrapper.orderByAsc(Problem::getId);
        }
        Page<Problem> page = problemMapper.selectPage(
                new Page<>(Math.max(query.getPage(), 1), Math.max(query.getSize(), 1)), wrapper);

        Map<Long, ContestVO> runningContests = contestService.runningContestsByProblem();
        List<ProblemVO> records = new ArrayList<>();
        for (Problem problem : page.getRecords()) {
            ProblemVO vo = ProblemVO.from(problem);
            if (userId != null) {
                vo.setMyStatus(resolveStatus(problem.getId(), accepted, attempted));
            }
            fillContest(vo, runningContests);
            records.add(vo);
        }
        return new PageResult<>(records, page.getTotal());
    }

    @Override
    public ProblemVO detail(Long id, Long userId, boolean admin) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在或已删除");
        }
        if (!admin && Integer.valueOf(0).equals(problem.getEnabled())) {
            throw new BusinessException(404, "题目不存在或已下线");
        }

        ProblemVO vo = ProblemVO.from(problem);
        fillContest(vo, contestService.runningContestsByProblem());
        if (userId != null) {
            List<Submission> mine = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getUserId, userId)
                    .eq(Submission::getProblemId, id)
                    .select(Submission::getProblemId, Submission::getStatus));
            Set<Long> accepted = new HashSet<>();
            Set<Long> attempted = new HashSet<>();
            for (Submission item : mine) {
                attempted.add(item.getProblemId());
                if (Integer.valueOf(STATUS_ACCEPTED).equals(item.getStatus())) {
                    accepted.add(item.getProblemId());
                }
            }
            vo.setMyStatus(resolveStatus(id, accepted, attempted));
        }
        return vo;
    }

    @Override
    public Long save(ProblemSaveRequest request, Long operatorId) {
        Problem problem;
        if (request.getId() != null) {
            problem = problemMapper.selectById(request.getId());
            if (problem == null) {
                throw new BusinessException(404, "题目不存在或已删除");
            }
        } else {
            problem = new Problem();
            problem.setSubmitCount(0);
            problem.setAcceptCount(0);
            problem.setEnabled(1);
            problem.setCreateBy(operatorId);
        }

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setTags(request.getTags() == null ? "" : String.join(",", request.getTags()));
        problem.setTimeLimit(request.getTimeLimit());
        problem.setMemoryLimit(request.getMemoryLimit());
        if (problem.getId() == null) {
            problemMapper.insert(problem);
        } else {
            problemMapper.updateById(problem);
        }
        return problem.getId();
    }

    @Override
    public void setEnabled(Long id, Integer enabled) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在或已删除");
        }
        problem.setEnabled(Integer.valueOf(0).equals(enabled) ? 0 : 1);
        problemMapper.updateById(problem);
    }

    @Override
    public List<ProblemCase> listCases(Long problemId) {
        return caseMapper.selectList(new LambdaQueryWrapper<ProblemCase>()
                .eq(ProblemCase::getProblemId, problemId)
                .orderByAsc(ProblemCase::getSortOrder));
    }

    @Override
    @Transactional
    public void saveCases(Long problemId, List<ProblemCaseRequest> cases) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在或已删除");
        }
        // 用例整组覆盖保存：先清空该题目的旧用例，再按顺序写入
        caseMapper.delete(new LambdaQueryWrapper<ProblemCase>().eq(ProblemCase::getProblemId, problemId));
        int order = 1;
        for (ProblemCaseRequest item : cases) {
            if (!StringUtils.hasText(item.getInput()) || !StringUtils.hasText(item.getOutput())) {
                throw new BusinessException("用例的输入与输出不能为空");
            }
            ProblemCase entity = new ProblemCase();
            entity.setProblemId(problemId);
            entity.setInput(item.getInput());
            entity.setOutput(item.getOutput());
            entity.setEnabled(item.getEnabled() == null ? 1 : item.getEnabled());
            entity.setSortOrder(order++);
            caseMapper.insert(entity);
        }
    }

    @Override
    public void delete(Long id) {
        Problem problem = problemMapper.selectById(id);
        if (problem == null) {
            throw new BusinessException(404, "题目不存在或已删除");
        }
        // 逻辑删除：题面与用例不再对外可见，历史提交记录仍可查看
        problemMapper.deleteById(id);
    }

    private String resolveStatus(Long problemId, Set<Long> accepted, Set<Long> attempted) {
        if (accepted.contains(problemId)) {
            return "ac";
        }
        return attempted.contains(problemId) ? "wa" : null;
    }

    /** 题目属于正在进行的竞赛时，补充竞赛信息，便于前端提示与提交归属 */
    private void fillContest(ProblemVO vo, Map<Long, ContestVO> runningContests) {
        ContestVO contest = runningContests.get(vo.getId());
        if (contest != null) {
            vo.setContestId(contest.getId());
            vo.setContestTitle(contest.getTitle());
        }
    }
}
