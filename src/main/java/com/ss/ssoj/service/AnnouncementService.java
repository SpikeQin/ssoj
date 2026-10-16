package com.ss.ssoj.service;

import com.ss.ssoj.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    /** 公告列表，按发布时间倒序 */
    List<Announcement> list();

    /** 新增或更新公告 */
    void save(Announcement announcement);

    /** 删除公告（逻辑删除） */
    void delete(Long id);
}
