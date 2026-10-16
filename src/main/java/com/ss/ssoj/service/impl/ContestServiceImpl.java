package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ss.ssoj.common.BusinessException;
import com.ss.ssoj.dto.ContestSaveRequest;
import com.ss.ssoj.entity.Contest;
import com.ss.ssoj.entity.ContestProblem;
import com.ss.ssoj.entity.Problem;
import com.ss.ssoj.entity.Submission;
import com.ss.ssoj.mapper.ContestMapper;
import com.ss.ssoj.mapper.ContestProblemMapper;
import com.ss.ssoj.mapper.ProblemMapper;
import com.ss.ssoj.mapper.StatsMapper;
import com.ss.ssoj.mapper.SubmissionMapper;
import com.ss.ssoj.mapper.SysUserMapper;
import com.ss.ssoj.entity.SysUser;
import com.ss.ssoj.service.ContestService;
import com.ss.ssoj.vo.ContestCellVO;
import com.ss.ssoj.vo.ContestProblemVO;
import com.ss.ssoj.vo.ContestRankRowVO;
import com.ss.ssoj.vo.ContestRankVO;
import com.ss.ssoj.vo.ContestStatVO;
import com.ss.ssoj.vo.ContestVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContestServiceImpl implements ContestService {

    /** 判题状态：通过 */
    private static final int STATUS_ACCEPTED = 2;
    /** 每次错误提交的罚时，单位分钟 */
    private static final int PENALTY_MINUTES = 20;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ContestMapper contestMapper;
    private final ContestProblemMapper contestProblemMapper;
    private final ProblemMapper problemMapper;
    private final SubmissionMapper submissionMapper;
    private final StatsMapper statsMapper;
    private final SysUserMapper userMapper;

    public ContestServiceImpl(ContestMapper contestMapper, ContestProblemMapper contestProblemMapper,
                              ProblemMapper problemMapper, SubmissionMapper submissionMapper,
                              StatsMapper statsMapper, SysUserMapper userMapper) {
        this.contestMapper = contestMapper;
        this.contestProblemMapper = contestProblemMapper;
        this.problemMapper = problemMapper;
        this.submissionMapper = submissionMapper;
        this.statsMapper = statsMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<ContestVO> list() {
        List<Contest> contests = contestMapper.selectList(new LambdaQueryWrapper<Contest>()
                .orderByDesc(Contest::getStartTime));
        if (contests.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> contestIds = contests.stream().map(Contest::getId).collect(Collectors.toSet());
        Map<Long, Integer> problemCounts = new HashMap<>();
        for (ContestProblem item : contestProblemMapper.selectList(
                new LambdaQueryWrapper<ContestProblem>().in(ContestProblem::getContestId, contestIds))) {
            problemCounts.merge(item.getContestId(), 1, Integer::sum);
        }
        Map<Long, Integer> participants = new HashMap<>();
        for (ContestStatVO item : statsMapper.selectContestParticipants()) {
            if (item.getContestId() != null) {
                participants.put(item.getContestId(), item.getParticipants());
            }
        }

        List<ContestVO> result = new ArrayList<>();
        for (Contest contest : contests) {
            result.add(toVo(contest, problemCounts.getOrDefault(contest.getId(), 0),
                    participants.getOrDefault(contest.getId(), 0)));
        }
        return result;
    }

    @Override
    public Map<String, Object> detail(Long id) {
        Contest contest = contestMapper.selectById(id);
        if (contest == null) {
            throw new BusinessException(404, "竞赛不存在或已删除");
        }

        List<ContestProblem> relations = contestProblemMapper.selectList(
                new LambdaQueryWrapper<ContestProblem>()
                        .eq(ContestProblem::getContestId, id)
                        .orderByAsc(ContestProblem::getSortOrder));
        Map<Long, Problem> problemMap = new HashMap<>();
        if (!relations.isEmpty()) {
            Set<Long> problemIds = relations.stream().map(ContestProblem::getProblemId).collect(Collectors.toSet());
            for (Problem problem : problemMapper.selectBatchIds(problemIds)) {
                problemMap.put(problem.getId(), problem);
            }
        }
        Map<Long, Integer> solvedMap = solvedCountByProblem(contest);

        List<ContestProblemVO> problems = new ArrayList<>();
        for (ContestProblem relation : relations) {
            Problem problem = problemMap.get(relation.getProblemId());
            ContestProblemVO vo = new ContestProblemVO();
            vo.setLabel(relation.getDisplayLabel());
            vo.setProblemId(relation.getProblemId());
            vo.setTitle(problem == null ? "已删除的题目" : problem.getTitle());
            vo.setDifficulty(problem == null ? 1 : problem.getDifficulty());
            vo.setSolved(solvedMap.getOrDefault(relation.getProblemId(), 0));
            problems.add(vo);
        }

        ContestVO contestVO = toVo(contest, relations.size(), distinctParticipants(contest));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", contestVO.getId());
        result.put("title", contestVO.getTitle());
        result.put("description", contestVO.getDescription());
        result.put("startTime", contestVO.getStartTime());
        result.put("endTime", contestVO.getEndTime());
        result.put("status", contestVO.getStatus());
        result.put("problemCount", contestVO.getProblemCount());
        result.put("participants", contestVO.getParticipants());
        result.put("hideProblems", contestVO.getHideProblems());
        result.put("problems", problems);
        return result;
    }

    @Override
    public Map<Long, ContestVO> runningContestsByProblem() {
        LocalDateTime now = LocalDateTime.now();
        List<Contest> running = contestMapper.selectList(new LambdaQueryWrapper<Contest>()
                .le(Contest::getStartTime, now)
                .ge(Contest::getEndTime, now)
                .orderByAsc(Contest::getStartTime));
        Map<Long, ContestVO> result = new HashMap<>();
        if (running.isEmpty()) {
            return result;
        }

        Map<Long, Contest> contestById = new HashMap<>();
        for (Contest contest : running) {
            contestById.put(contest.getId(), contest);
        }
        List<ContestProblem> relations = contestProblemMapper.selectList(
                new LambdaQueryWrapper<ContestProblem>().in(ContestProblem::getContestId, contestById.keySet()));
        for (Contest contest : running) {
            // 已按开始时间升序，开始较晚的竞赛覆盖较早的，保证每题只归属一场竞赛
            for (ContestProblem relation : relations) {
                if (relation.getContestId().equals(contest.getId())) {
                    result.put(relation.getProblemId(), toVo(contest, 0, 0));
                }
            }
        }
        return result;
    }

    @Override
    public Set<Long> hiddenProblemIds() {
        List<Contest> contests = contestMapper.selectList(new LambdaQueryWrapper<Contest>()
                .eq(Contest::getHideProblems, 1)
                .ge(Contest::getEndTime, LocalDateTime.now()));
        if (contests.isEmpty()) {
            return Set.of();
        }
        List<ContestProblem> relations = contestProblemMapper.selectList(new LambdaQueryWrapper<ContestProblem>()
                .in(ContestProblem::getContestId, contests.stream().map(Contest::getId).toList()));
        return relations.stream().map(ContestProblem::getProblemId).collect(Collectors.toSet());
    }

    @Override
    public ContestRankVO rank(Long id) {
        Contest contest = contestMapper.selectById(id);
        if (contest == null) {
            throw new BusinessException(404, "竞赛不存在或已删除");
        }
        List<ContestProblem> relations = contestProblemMapper.selectList(
                new LambdaQueryWrapper<ContestProblem>()
                        .eq(ContestProblem::getContestId, id)
                        .orderByAsc(ContestProblem::getSortOrder));
        List<String> labels = relations.stream().map(ContestProblem::getDisplayLabel).toList();

        ContestRankVO result = new ContestRankVO();
        result.setLabels(new ArrayList<>(labels));
        if (relations.isEmpty()) {
            return result;
        }

        // 只统计竞赛时间窗内的提交，赛后补交不计入榜单
        List<Submission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, id)
                .ge(Submission::getSubmitTime, contest.getStartTime())
                .le(Submission::getSubmitTime, contest.getEndTime())
                .orderByAsc(Submission::getId));
        Map<String, Long> labelByProblem = new HashMap<>();
        for (ContestProblem relation : relations) {
            labelByProblem.put(relation.getDisplayLabel(), relation.getProblemId());
        }

        Map<Long, ContestRankRowVO> rows = new LinkedHashMap<>();
        Map<Long, Map<Long, int[]>> counter = new HashMap<>();
        for (Submission submission : submissions) {
            ContestRankRowVO row = rows.computeIfAbsent(submission.getUserId(), userId -> {
                ContestRankRowVO item = new ContestRankRowVO();
                item.setUserId(userId);
                SysUser user = userMapper.selectById(userId);
                item.setNickname(user == null ? "未知用户" : user.getNickname());
                for (String label : labels) {
                    item.getCells().put(label, new ContestCellVO());
                }
                return item;
            });
            Map<Long, int[]> perProblem = counter.computeIfAbsent(submission.getUserId(), k -> new HashMap<>());
            int[] state = perProblem.computeIfAbsent(submission.getProblemId(), k -> new int[]{0, 0});
            // state[0] 是否已通过，state[1] 错误提交次数
            if (state[0] == 1) {
                continue;
            }
            if (Integer.valueOf(STATUS_ACCEPTED).equals(submission.getStatus())) {
                state[0] = 1;
                row.setSolved(row.getSolved() == null ? 1 : row.getSolved() + 1);
                long minutes = Duration.between(contest.getStartTime(), submission.getSubmitTime()).toMinutes();
                row.setPenalty((row.getPenalty() == null ? 0 : row.getPenalty())
                        + (int) Math.max(minutes, 0) + PENALTY_MINUTES * state[1]);
            } else {
                state[1]++;
            }
            String label = findLabel(relations, submission.getProblemId());
            if (label != null) {
                ContestCellVO cell = row.getCells().get(label);
                if (cell != null) {
                    cell.setAccepted(state[0] == 1);
                    cell.setTries(cell.getTries() + 1);
                }
            }
        }

        List<ContestRankRowVO> records = new ArrayList<>(rows.values());
        records.forEach(row -> {
            if (row.getSolved() == null) {
                row.setSolved(0);
            }
            if (row.getPenalty() == null) {
                row.setPenalty(0);
            }
        });
        records.sort(Comparator.comparingInt(ContestRankRowVO::getSolved).reversed()
                .thenComparingInt(ContestRankRowVO::getPenalty));
        for (int index = 0; index < records.size(); index++) {
            records.get(index).setRank(index + 1);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    @Transactional
    public Long save(ContestSaveRequest request, Long operatorId) {
        LocalDateTime start = parseTime(request.getStartTime(), "开始时间");
        LocalDateTime end = parseTime(request.getEndTime(), "结束时间");
        if (!end.isAfter(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        Contest contest = new Contest();
        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setStartTime(start);
        contest.setEndTime(end);
        contest.setHideProblems(Integer.valueOf(0).equals(request.getHideProblems()) ? 0 : 1);
        contest.setCreateBy(operatorId);
        contestMapper.insert(contest);

        int order = 1;
        for (Long problemId : request.getProblemIds()) {
            if (problemMapper.selectById(problemId) == null) {
                throw new BusinessException(404, "题目不存在：" + problemId);
            }
            ContestProblem relation = new ContestProblem();
            relation.setContestId(contest.getId());
            relation.setProblemId(problemId);
            relation.setDisplayLabel(String.valueOf((char) ('A' + order - 1)));
            relation.setSortOrder(order++);
            contestProblemMapper.insert(relation);
        }
        return contest.getId();
    }

    @Override
    @Transactional
    public Long update(Long id, ContestSaveRequest request, Long operatorId) {
        Contest contest = contestMapper.selectById(id);
        if (contest == null) {
            throw new BusinessException(404, "竞赛不存在或已删除");
        }
        LocalDateTime start = parseTime(request.getStartTime(), "开始时间");
        LocalDateTime end = parseTime(request.getEndTime(), "结束时间");
        if (!end.isAfter(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setStartTime(start);
        contest.setEndTime(end);
        contest.setHideProblems(Integer.valueOf(0).equals(request.getHideProblems()) ? 0 : 1);
        contestMapper.updateById(contest);

        // 题目编排整组覆盖，重新按顺序分配 A、B、C 编号
        contestProblemMapper.delete(new LambdaQueryWrapper<ContestProblem>()
                .eq(ContestProblem::getContestId, id));
        int order = 1;
        for (Long problemId : request.getProblemIds()) {
            if (problemMapper.selectById(problemId) == null) {
                throw new BusinessException(404, "题目不存在：" + problemId);
            }
            ContestProblem relation = new ContestProblem();
            relation.setContestId(id);
            relation.setProblemId(problemId);
            relation.setDisplayLabel(String.valueOf((char) ('A' + order - 1)));
            relation.setSortOrder(order++);
            contestProblemMapper.insert(relation);
        }
        return id;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Contest contest = contestMapper.selectById(id);
        if (contest == null) {
            throw new BusinessException(404, "竞赛不存在或已删除");
        }
        // 先解除题目编排，再逻辑删除竞赛；赛事期间的提交记录继续保留
        contestProblemMapper.delete(new LambdaQueryWrapper<ContestProblem>()
                .eq(ContestProblem::getContestId, id));
        contestMapper.deleteById(id);
    }

    private Map<Long, Integer> solvedCountByProblem(Contest contest) {
        Map<Long, Integer> result = new HashMap<>();
        List<Submission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contest.getId())
                .eq(Submission::getStatus, STATUS_ACCEPTED)
                .ge(Submission::getSubmitTime, contest.getStartTime())
                .le(Submission::getSubmitTime, contest.getEndTime())
                .select(Submission::getUserId, Submission::getProblemId));
        Map<Long, Set<Long>> usersByProblem = new HashMap<>();
        for (Submission submission : submissions) {
            usersByProblem.computeIfAbsent(submission.getProblemId(), k -> new HashSet<>())
                    .add(submission.getUserId());
        }
        usersByProblem.forEach((problemId, users) -> result.put(problemId, users.size()));
        return result;
    }

    private int distinctParticipants(Contest contest) {
        List<Submission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getContestId, contest.getId())
                .ge(Submission::getSubmitTime, contest.getStartTime())
                .le(Submission::getSubmitTime, contest.getEndTime())
                .select(Submission::getUserId));
        return (int) submissions.stream().map(Submission::getUserId).distinct().count();
    }

    private String findLabel(List<ContestProblem> relations, Long problemId) {
        for (ContestProblem relation : relations) {
            if (relation.getProblemId().equals(problemId)) {
                return relation.getDisplayLabel();
            }
        }
        return null;
    }

    private ContestVO toVo(Contest contest, int problemCount, int participants) {
        ContestVO vo = new ContestVO();
        vo.setId(contest.getId());
        vo.setTitle(contest.getTitle());
        vo.setDescription(contest.getDescription());
        vo.setStartTime(contest.getStartTime());
        vo.setEndTime(contest.getEndTime());
        vo.setProblemCount(problemCount);
        vo.setParticipants(participants);
        vo.setHideProblems(contest.getHideProblems() == null ? 1 : contest.getHideProblems());
        vo.setStatus(resolveStatus(contest));
        return vo;
    }

    private String resolveStatus(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime())) {
            return "before";
        }
        return now.isAfter(contest.getEndTime()) ? "finished" : "running";
    }

    private LocalDateTime parseTime(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("请选择" + field);
        }
        try {
            return LocalDateTime.parse(value.trim().replace('T', ' '), FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(field + "格式应为 yyyy-MM-dd HH:mm:ss");
        }
    }
}
