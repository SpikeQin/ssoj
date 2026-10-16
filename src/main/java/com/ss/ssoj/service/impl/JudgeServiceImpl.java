package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import tools.jackson.databind.ObjectMapper;
import com.ss.ssoj.common.JudgeStatus;
import com.ss.ssoj.common.JudgeOutputComparator;
import com.ss.ssoj.config.JudgeProperties;
import com.ss.ssoj.entity.Problem;
import com.ss.ssoj.entity.ProblemCase;
import com.ss.ssoj.entity.Submission;
import com.ss.ssoj.mapper.ProblemCaseMapper;
import com.ss.ssoj.mapper.ProblemMapper;
import com.ss.ssoj.mapper.SubmissionMapper;
import com.ss.ssoj.service.JudgeService;
import com.ss.ssoj.vo.CaseResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 判题引擎：提交进入固定大小的线程池，在线程中完成编译、逐用例运行与结果比对。
 *
 * 每个提交使用独立的工作目录，运行时间超过题目限制即强制结束进程，
 * 输出超过上限时同样结束进程并判为运行错误。
 */
@Service
public class JudgeServiceImpl implements JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeServiceImpl.class);

    private final JudgeProperties properties;
    private final SubmissionMapper submissionMapper;
    private final ProblemMapper problemMapper;
    private final ProblemCaseMapper caseMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ThreadPoolExecutor executor;

    public JudgeServiceImpl(JudgeProperties properties, SubmissionMapper submissionMapper,
                            ProblemMapper problemMapper, ProblemCaseMapper caseMapper) {
        this.properties = properties;
        this.submissionMapper = submissionMapper;
        this.problemMapper = problemMapper;
        this.caseMapper = caseMapper;
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(Math.max(properties.getQueueSize(), 1));
        this.executor = new ThreadPoolExecutor(
                Math.max(properties.getThreads(), 1),
                Math.max(properties.getThreads(), 1),
                0L, TimeUnit.MILLISECONDS, queue, new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void submit(Long submissionId) {
        try {
            executor.execute(() -> judge(submissionId));
        } catch (RejectedExecutionException ex) {
            log.warn("判题队列已满，提交 {} 未能入队", submissionId);
            finish(submissionId, JudgeStatus.SYSTEM_ERROR, null, "判题队列已满，请重新提交", List.of());
        }
    }

    @Override
    public boolean isQueueFull() {
        return executor.getQueue().remainingCapacity() <= 0;
    }

    @Override
    public void requeueUnfinished() {
        List<Submission> unfinished = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .in(Submission::getStatus, JudgeStatus.PENDING, JudgeStatus.JUDGING)
                .orderByAsc(Submission::getId));
        for (Submission item : unfinished) {
            submit(item.getId());
        }
        if (!unfinished.isEmpty()) {
            log.info("服务启动，重新入队 {} 条未完成的提交", unfinished.size());
        }
    }

    @Override
    public void cleanWorkspace() {
        Path workspace = Path.of(properties.getWorkspace());
        if (!Files.isDirectory(workspace)) {
            return;
        }
        try (Stream<Path> paths = Files.list(workspace)) {
            paths.filter(Files::isDirectory).forEach(this::deleteWorkspace);
        } catch (IOException ex) {
            log.warn("判题工作目录清理失败：{}", workspace, ex);
        }
    }

    private void judge(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            return;
        }
        markJudging(submissionId);

        Path workspace = null;
        try {
            workspace = Files.createDirectories(
                    Path.of(properties.getWorkspace(), "submission-" + submissionId));
            JudgeProperties.Language language = properties.getLanguages().get(submission.getLanguage());
            if (language == null) {
                finish(submissionId, JudgeStatus.SYSTEM_ERROR, null,
                        "不支持的语言：" + submission.getLanguage(), List.of());
                return;
            }

            Files.writeString(workspace.resolve(language.getSourceFile()),
                    submission.getCode(), StandardCharsets.UTF_8);

            if (StringUtils.hasText(language.getCompileCommand())) {
                ExecResult compile = exec(workspace, language.getCompileCommand(), "",
                        properties.getCompileTimeoutSeconds() * 1000);
                if (compile.timedOut()) {
                    finish(submissionId, JudgeStatus.COMPILE_ERROR, null,
                            "编译超时（超过 " + properties.getCompileTimeoutSeconds() + " 秒）", List.of());
                    return;
                }
                if (compile.exitCode() != 0) {
                    finish(submissionId, JudgeStatus.COMPILE_ERROR, null,
                            limit(compile.output(), 2000), List.of());
                    return;
                }
            }

            Problem problem = problemMapper.selectById(submission.getProblemId());
            if (problem == null) {
                finish(submissionId, JudgeStatus.SYSTEM_ERROR, null, "题目已被删除", List.of());
                return;
            }
            List<ProblemCase> cases = caseMapper.selectList(new LambdaQueryWrapper<ProblemCase>()
                    .eq(ProblemCase::getProblemId, submission.getProblemId())
                    .eq(ProblemCase::getEnabled, 1)
                    .orderByAsc(ProblemCase::getSortOrder));
            if (cases.isEmpty()) {
                finish(submissionId, JudgeStatus.SYSTEM_ERROR, null, "该题目没有启用的测试用例", List.of());
                return;
            }

            List<CaseResultVO> results = new ArrayList<>();
            int finalStatus = JudgeStatus.ACCEPTED;
            int maxTime = 0;
            for (ProblemCase testCase : cases) {
                ExecResult run = exec(workspace, language.getRunCommand(), testCase.getInput(),
                        problem.getTimeLimit());
                CaseResultVO result = evaluate(testCase, run, problem.getTimeLimit());
                // 用例编号采用执行顺序（从 1 开始），数据库主键对使用者没有意义
                result.setCaseId((long) results.size() + 1);
                results.add(result);
                maxTime = Math.max(maxTime, result.getTimeUsed() == null ? 0 : result.getTimeUsed());
                if (result.getStatus() != JudgeStatus.ACCEPTED) {
                    // 首个未通过的用例即停止判题，与常见在线评测行为一致
                    finalStatus = result.getStatus();
                    break;
                }
            }

            finish(submissionId, finalStatus, maxTime, null, results);
            if (finalStatus == JudgeStatus.ACCEPTED) {
                problemMapper.update(null, new LambdaUpdateWrapper<Problem>()
                        .eq(Problem::getId, problem.getId())
                        .setSql("accept_count = accept_count + 1"));
            }
        } catch (Exception ex) {
            log.error("判题异常 submissionId={}", submissionId, ex);
            finish(submissionId, JudgeStatus.SYSTEM_ERROR, null,
                    "判题过程出现异常：" + ex.getMessage(), List.of());
        } finally {
            deleteWorkspace(workspace);
        }
    }

    private CaseResultVO evaluate(ProblemCase testCase, ExecResult run, int timeLimit) {
        if (run.outputOverflow()) {
            return new CaseResultVO(testCase.getId(), JudgeStatus.RUNTIME_ERROR, run.timeMs(),
                    "程序输出超出限制");
        }
        if (run.timedOut()) {
            return new CaseResultVO(testCase.getId(), JudgeStatus.TIME_LIMIT_EXCEEDED, timeLimit,
                    "运行超过时间限制 " + timeLimit + " ms");
        }
        if (run.exitCode() != 0) {
            String detail = StringUtils.hasText(run.output()) ? "：" + limit(run.output(), 200) : "";
            return new CaseResultVO(testCase.getId(), JudgeStatus.RUNTIME_ERROR, run.timeMs(),
                    "运行错误（退出码 " + run.exitCode() + "）" + detail);
        }
        if (!JudgeOutputComparator.matches(testCase.getOutput(), run.output())) {
            return new CaseResultVO(testCase.getId(), JudgeStatus.WRONG_ANSWER, run.timeMs(),
                    "输出与标准答案不一致");
        }
        return new CaseResultVO(testCase.getId(), JudgeStatus.ACCEPTED, run.timeMs(), "通过");
    }

    private ExecResult exec(Path workspace, String command, String input, int timeoutMs)
            throws IOException, InterruptedException {
        List<String> parts = new ArrayList<>(Arrays.asList(command.trim().split("\\s+")));
        // 编译产物位于工作目录中，必须使用绝对路径：子进程的工作目录不会改变程序文件的查找位置
        if (parts.size() == 1 && !parts.get(0).contains("\\") && !parts.get(0).contains("/")) {
            Path executable = workspace.resolve(parts.get(0));
            if (isWindows() && !Files.exists(executable)) {
                Path withExe = workspace.resolve(parts.get(0) + ".exe");
                if (Files.exists(withExe)) {
                    executable = withExe;
                }
            }
            if (Files.exists(executable)) {
                parts.set(0, executable.toAbsolutePath().toString());
            }
        }

        Path inputFile = workspace.resolve("_stdin.txt");
        Files.writeString(inputFile, input == null ? "" : input, StandardCharsets.UTF_8);

        ProcessBuilder builder = new ProcessBuilder(parts);
        builder.directory(workspace.toFile());
        builder.redirectInput(inputFile.toFile());
        Process process = builder.start();

        Capture stdout = new Capture();
        Capture stderr = new Capture();
        int limit = Math.max(properties.getOutputLimitKb(), 1) * 1024;
        Thread outThread = new Thread(() -> pump(process.getInputStream(), stdout, limit, process));
        Thread errThread = new Thread(() -> pump(process.getErrorStream(), stderr, limit, process));
        outThread.setDaemon(true);
        errThread.setDaemon(true);
        outThread.start();
        errThread.start();

        long start = System.currentTimeMillis();
        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(2, TimeUnit.SECONDS);
        }
        long elapsed = System.currentTimeMillis() - start;
        outThread.join(1000);
        errThread.join(1000);

        boolean overflow = stdout.overflow || stderr.overflow;
        String output = stdout.text.toString();
        if (!StringUtils.hasText(output) && StringUtils.hasText(stderr.text.toString())) {
            // 程序把错误信息输出到了标准错误，一并作为运行信息返回
            output = stderr.text.toString();
        }
        int exitCode = finished ? process.exitValue() : -1;
        return new ExecResult(exitCode, (int) Math.min(elapsed, Integer.MAX_VALUE),
                output, stderr.text.toString(), !finished, overflow);
    }

    private void pump(InputStream stream, Capture capture, int limit, Process process) {
        try (InputStream input = stream) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (capture.overflow) {
                    continue;
                }
                if (capture.text.length() + read > limit) {
                    capture.overflow = true;
                    process.destroyForcibly();
                    continue;
                }
                capture.text.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            // 进程被强制结束时读取会中断，属于正常情况
        }
    }

    private void markJudging(Long submissionId) {
        submissionMapper.update(null, new LambdaUpdateWrapper<Submission>()
                .eq(Submission::getId, submissionId)
                .set(Submission::getStatus, JudgeStatus.JUDGING)
                .set(Submission::getTimeUsed, null)
                .set(Submission::getErrorMessage, null)
                .set(Submission::getCaseDetail, null));
    }

    private void finish(Long submissionId, int status, Integer timeUsed,
                        String errorMessage, List<CaseResultVO> cases) {
        String caseDetail = null;
        if (cases != null && !cases.isEmpty()) {
            try {
                caseDetail = objectMapper.writeValueAsString(cases);
            } catch (Exception ex) {
                log.warn("用例结果序列化失败 submissionId={}", submissionId, ex);
            }
        }
        submissionMapper.update(null, new LambdaUpdateWrapper<Submission>()
                .eq(Submission::getId, submissionId)
                .set(Submission::getStatus, status)
                .set(Submission::getTimeUsed, timeUsed)
                .set(Submission::getErrorMessage, errorMessage)
                .set(Submission::getCaseDetail, caseDetail)
                .set(Submission::getJudgeTime, LocalDateTime.now()));
    }

    private void deleteWorkspace(Path workspace) {
        if (workspace == null || !Files.exists(workspace)) {
            return;
        }
        // 超时被强制结束的进程可能仍短暂占用可执行文件，删除失败时重试一次
        for (int attempt = 0; attempt < 2; attempt++) {
            try (Stream<Path> paths = Files.walk(workspace)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // 单个文件删除失败不影响判题结果
                    }
                });
            } catch (IOException ex) {
                log.warn("判题工作目录清理失败：{}", workspace, ex);
            }
            if (!Files.exists(workspace)) {
                return;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (Files.exists(workspace)) {
            log.warn("判题工作目录未能删除，将在下次启动时清理：{}", workspace);
        }
    }

    private String limit(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String trimmed = text.strip();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength) + "...";
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static final class Capture {
        private final StringBuilder text = new StringBuilder();
        private volatile boolean overflow = false;
    }

    private record ExecResult(int exitCode, int timeMs, String output, String error,
                              boolean timedOut, boolean outputOverflow) {
    }
}


