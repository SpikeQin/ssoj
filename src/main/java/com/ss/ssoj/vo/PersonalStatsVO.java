package com.ss.ssoj.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人中心的统计数据，statusCounts 以状态名（ac、wa 等）为键。
 */
@Data
public class PersonalStatsVO {

    private Integer submitCount;
    private Integer acCount;
    private Integer acceptRate;
    private Map<String, Integer> statusCounts = new LinkedHashMap<>();
    private List<SubmissionVO> recent;
}
