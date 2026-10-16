package com.ss.ssoj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ContestSaveRequest {

    @NotBlank(message = "请输入竞赛名称")
    private String title;

    private String description;

    @NotBlank(message = "请选择开始时间")
    private String startTime;

    @NotBlank(message = "请选择结束时间")
    private String endTime;

    @NotEmpty(message = "请至少选择一道题目")
    private List<Long> problemIds;

    /** 竞赛未结束时是否在学生题库中隐藏赛题，为空时按隐藏处理 */
    private Integer hideProblems;
}
