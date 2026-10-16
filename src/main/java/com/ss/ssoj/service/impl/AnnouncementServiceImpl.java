package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ss.ssoj.common.BusinessException;
import com.ss.ssoj.entity.Announcement;
import com.ss.ssoj.mapper.AnnouncementMapper;
import com.ss.ssoj.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public AnnouncementServiceImpl(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @Override
    public List<Announcement> list() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime));
    }

    @Override
    public void save(Announcement announcement) {
        if (!StringUtils.hasText(announcement.getTitle())
                || !StringUtils.hasText(announcement.getContent())) {
            throw new BusinessException("公告标题与内容不能为空");
        }
        if (announcement.getId() == null) {
            announcementMapper.insert(announcement);
        } else {
            announcementMapper.updateById(announcement);
        }
    }

    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }
}
