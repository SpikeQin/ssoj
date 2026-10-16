package com.ss.ssoj.controller;

import com.ss.ssoj.auth.RequireAdmin;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.entity.SysConfig;
import com.ss.ssoj.service.SysConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * 系统配置接口：配置只服务管理后台，读取与修改都仅管理员可操作。
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final SysConfigService configService;

    public SystemController(SysConfigService configService) {
        this.configService = configService;
    }

    @RequireAdmin
    @GetMapping("/config/list")
    public Result<List<SysConfig>> configList() {
        return Result.success(configService.list());
    }

    @RequireAdmin
    @PutMapping("/config")
    public Result<Void> saveConfig(@RequestBody List<SysConfig> list) {
        configService.saveAll(list);
        return Result.success();
    }
}
