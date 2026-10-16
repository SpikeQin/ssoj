package com.ss.ssoj.vo;

import lombok.Data;

/**
 * 竞赛参与人数统计，用于竞赛列表展示。
 */
@Data
public class ContestStatVO {

    private Long contestId;
    private Integer participants;
}
