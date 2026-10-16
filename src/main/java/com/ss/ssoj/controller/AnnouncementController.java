package com.ss.ssoj.controller;

import com.ss.ssoj.auth.Public;
import com.ss.ssoj.auth.RequireAdmin;
import com.ss.ssoj.auth.UserContext;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.entity.Announcement;
import com.ss.ssoj.service.AnnouncementService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 公告接口：列表公开，维护仅管理员可操作。 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @Public
    @GetMapping
    public Result<List<Announcement>> list() {
        return Result.success(announcementService.list());
    }

    @RequireAdmin
    @PostMapping
    public Result<Void> create(@RequestBody Announcement announcement) {
        announcement.setId(null);
        announcement.setCreateBy(UserContext.userId());
        announcementService.save(announcement);
        return Result.success();
    }

    @RequireAdmin
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.save(announcement);
        return Result.success();
    }

    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
