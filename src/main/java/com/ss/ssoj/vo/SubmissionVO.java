package com.ss.ssoj.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提交记录列表项。
 */
@Data
public class SubmissionVO {

    private Long id;
    private Long userId;
    private String nickname;
    private Long problemId;
    private String problemTitle;
    private Long contestId;
    private String language;
    private Integer status;
    private Integer timeUsed;
    private Integer codeLength;

    private LocalDateTime submitTime;
}
