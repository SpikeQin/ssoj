package com.ss.ssoj.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛信息。status 取 before（未开始）、running（进行中）、finished（已结束）。
 */
@Data
public class ContestVO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer problemCount;
    private Integer participants;
    /** 竞赛未结束时赛题是否不在学生题库出现：0-否，1-是 */
    private Integer hideProblems;
}
