package com.ss.ssoj.controller;

import com.ss.ssoj.auth.Public;
import com.ss.ssoj.auth.RequireAdmin;
import com.ss.ssoj.auth.UserContext;
import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.dto.ProblemCaseRequest;
import com.ss.ssoj.dto.ProblemQuery;
import com.ss.ssoj.dto.ProblemSaveRequest;
import com.ss.ssoj.entity.ProblemCase;
import com.ss.ssoj.service.ProblemService;
import com.ss.ssoj.vo.ProblemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 题目接口：列表与详情对游客开放，维护类接口仅管理员可访问。 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @Public
    @GetMapping
    public Result<PageResult<ProblemVO>> list(ProblemQuery query) {
        boolean admin = UserContext.isAdmin();
        return Result.success(problemService.page(query, UserContext.userId(), admin));
    }

    @Public
    @GetMapping("/{id}")
    public Result<ProblemVO> detail(@PathVariable Long id) {
        return Result.success(problemService.detail(id, UserContext.userId(), UserContext.isAdmin()));
    }

    @RequireAdmin
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProblemSaveRequest request) {
        request.setId(null);
        return Result.success(problemService.save(request, UserContext.userId()));
    }

    @RequireAdmin
    @PutMapping("/{id}")
    public Result<Long> update(@PathVariable Long id, @Valid @RequestBody ProblemSaveRequest request) {
        request.setId(id);
        return Result.success(problemService.save(request, UserContext.userId()));
    }

    @RequireAdmin
    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id, @RequestParam Integer enabled) {
        problemService.setEnabled(id, enabled);
        return Result.success();
    }

    @RequireAdmin
    @GetMapping("/{id}/cases")
    public Result<List<ProblemCase>> listCases(@PathVariable Long id) {
        return Result.success(problemService.listCases(id));
    }

    @RequireAdmin
    @PostMapping("/{id}/cases")
    public Result<Void> saveCases(@PathVariable Long id,
                                  @RequestBody List<ProblemCaseRequest> cases) {
        problemService.saveCases(id, cases);
        return Result.success();
    }

    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        problemService.delete(id);
        return Result.success();
    }
}
