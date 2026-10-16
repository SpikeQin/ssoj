package com.ss.ssoj.controller;

import com.ss.ssoj.auth.Public;
import com.ss.ssoj.auth.RequireAdmin;
import com.ss.ssoj.auth.UserContext;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.dto.ContestSaveRequest;
import com.ss.ssoj.service.ContestService;
import com.ss.ssoj.vo.ContestRankVO;
import com.ss.ssoj.vo.ContestVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 竞赛接口：列表、详情与榜单对游客开放，创建仅管理员可操作。 */
@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @Public
    @GetMapping
    public Result<List<ContestVO>> list() {
        return Result.success(contestService.list());
    }

    @Public
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(contestService.detail(id));
    }

    @Public
    @GetMapping("/{id}/rank")
    public Result<ContestRankVO> rank(@PathVariable Long id) {
        return Result.success(contestService.rank(id));
    }

    @RequireAdmin
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ContestSaveRequest request) {
        return Result.success(contestService.save(request, UserContext.userId()));
    }

    @RequireAdmin
    @PutMapping("/{id}")
    public Result<Long> update(@PathVariable Long id, @Valid @RequestBody ContestSaveRequest request) {
        return Result.success(contestService.update(id, request, UserContext.userId()));
    }

    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contestService.delete(id);
        return Result.success();
    }
}
