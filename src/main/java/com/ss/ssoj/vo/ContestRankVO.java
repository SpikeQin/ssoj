package com.ss.ssoj.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 竞赛榜单：labels 为题目显示编号顺序，records 为按规则排序后的排名。
 */
@Data
public class ContestRankVO {

    private List<String> labels = new ArrayList<>();
    private List<ContestRankRowVO> records = new ArrayList<>();
}
