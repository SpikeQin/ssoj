package com.ss.ssoj.vo;

import lombok.Data;

/**
 * 竞赛内的题目，label 为竞赛内显示编号（A、B、C）。
 */
@Data
public class ContestProblemVO {

    private String label;
    private Long problemId;
    private String title;
    private Integer difficulty;
    private Integer solved;
}
