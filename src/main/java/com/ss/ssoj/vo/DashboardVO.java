package com.ss.ssoj.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理首页的概览数据。
 */
@Data
public class DashboardVO {

    private Integer userCount;
    private Integer problemCount;
    private Integer submissionCount;
    private Integer pendingCount;
    private Integer todaySubmission;
    private Integer passRate;
    private List<SubmissionVO> recent;
}
