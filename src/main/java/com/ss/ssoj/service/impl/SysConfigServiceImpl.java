package com.ss.ssoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ss.ssoj.entity.SysConfig;
import com.ss.ssoj.mapper.SysConfigMapper;
import com.ss.ssoj.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper configMapper;

    public SysConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public List<SysConfig> list() {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getId));
    }

    @Override
    public void saveAll(List<SysConfig> list) {
        for (SysConfig item : list) {
            if (item.getConfigKey() == null || item.getConfigValue() == null) {
                continue;
            }
            configMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, item.getConfigKey())
                    .set(SysConfig::getConfigValue, item.getConfigValue()));
        }
    }
}
