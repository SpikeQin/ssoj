package com.ss.ssoj.dto;

import lombok.Data;

/**
 * 提交记录查询条件。
 * scope 取 mine（我的提交）或 all（全部提交），默认 all。
 */
@Data
public class SubmissionQuery {

    private String scope = "all";

    private Long problemId;

    private Long userId;

    private Integer status;

    private String language;

    /** 按提交者搜索：匹配昵称或用户名（学号） */
    private String keyword;

    private Integer page = 1;

    private Integer size = 10;
}
