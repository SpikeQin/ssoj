package com.ss.ssoj.service;

import com.ss.ssoj.entity.SysConfig;

import java.util.List;

public interface SysConfigService {

    /** 后台配置列表，包含说明文字 */
    List<SysConfig> list();

    /** 批量保存配置值 */
    void saveAll(List<SysConfig> list);
}
