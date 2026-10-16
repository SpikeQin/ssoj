package com.ss.ssoj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 竞赛题目表：display_label 为竞赛内的显示编号，如 A、B、C。
 */
@Data
@TableName("contest_problem")
public class ContestProblem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contestId;

    private Long problemId;

    private String displayLabel;

    private Integer sortOrder;
}
