package com.ss.ssoj.controller;

import com.ss.ssoj.auth.RequireAdmin;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.service.DashboardService;
import com.ss.ssoj.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理首页概览接口，仅管理员可访问。 */
@RequireAdmin
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public Result<DashboardVO> summary() {
        return Result.success(dashboardService.summary());
    }
}
