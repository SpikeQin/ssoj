package com.ss.ssoj.controller;

import com.ss.ssoj.auth.UserContext;
import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.dto.SubmissionQuery;
import com.ss.ssoj.dto.SubmissionRequest;
import com.ss.ssoj.service.SubmissionService;
import com.ss.ssoj.vo.SubmissionDetailVO;
import com.ss.ssoj.vo.SubmissionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 提交与判题结果接口，全部需要登录。 */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public Result<Long> submit(@Valid @RequestBody SubmissionRequest request) {
        return Result.success(submissionService.submit(request, UserContext.userId()));
    }

    @GetMapping
    public Result<PageResult<SubmissionVO>> list(SubmissionQuery query) {
        return Result.success(submissionService.page(query, UserContext.userId(), UserContext.isAdmin()));
    }

    @GetMapping("/{id}")
    public Result<SubmissionDetailVO> detail(@PathVariable Long id) {
        return Result.success(submissionService.detail(id, UserContext.userId(), UserContext.isAdmin()));
    }
}
