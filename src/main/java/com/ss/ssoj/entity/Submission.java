package com.ss.ssoj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交记录表：contest_id 为空表示题库练习提交。
 */
@Data
@TableName("submission")
public class Submission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long problemId;

    private Long contestId;

    private String language;

    private String code;

    /** 判题状态：0-待判题，1-判题中，2-通过，3-答案错误，4-超时，5-运行错误，6-编译错误，7-系统错误 */
    private Integer status;

    private Integer timeUsed;

    private String errorMessage;

    private String caseDetail;

    private LocalDateTime submitTime;

    private LocalDateTime judgeTime;
}
