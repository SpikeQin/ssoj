package com.ss.ssoj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目表：主键即题目编号，标签以英文逗号分隔保存。
 */
@Data
@TableName("problem")
public class Problem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** 难度：1-简单，2-中等，3-困难 */
    private Integer difficulty;

    private String tags;

    private Integer timeLimit;

    private Integer memoryLimit;

    /** 状态：0-已下线，1-已上线 */
    private Integer enabled;

    private Integer submitCount;

    private Integer acceptCount;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
