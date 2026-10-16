package com.ss.ssoj.dto;

import lombok.Data;

/**
 * 题目列表查询条件。
 * status 取 ac（已通过）、wa（未通过）、none（未尝试），仅登录用户生效。
 */
@Data
public class ProblemQuery {

    private String keyword;

    private Integer difficulty;

    private String status;

    private Integer page = 1;

    private Integer size = 10;
}
