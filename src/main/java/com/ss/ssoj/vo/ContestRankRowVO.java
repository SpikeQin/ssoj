package com.ss.ssoj.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 竞赛榜单的一行，cells 以题目显示编号为键。
 */
@Data
public class ContestRankRowVO {

    private Integer rank;
    private Long userId;
    private String nickname;
    private Integer solved;
    private Integer penalty;
    private Map<String, ContestCellVO> cells = new LinkedHashMap<>();
}
