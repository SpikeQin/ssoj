package com.ss.ssoj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 测试用例表：题目的输入输出数据，按 sort_order 顺序执行。
 */
@Data
@TableName("problem_case")
public class ProblemCase {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;

    private String input;

    private String output;

    private Integer enabled;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
