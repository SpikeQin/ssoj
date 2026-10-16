package com.ss.ssoj.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProblemSaveRequest {

    private Long id;

    @NotBlank(message = "请输入题目标题")
    private String title;

    @NotBlank(message = "请输入题面描述")
    private String description;

    @NotNull(message = "请选择难度")
    @Min(value = 1, message = "难度取值为 1 到 3")
    @Max(value = 3, message = "难度取值为 1 到 3")
    private Integer difficulty;

    private List<String> tags;

    @Min(value = 100, message = "时间限制不能小于 100 毫秒")
    private Integer timeLimit = 1000;

    @Min(value = 16, message = "内存限制不能小于 16 MB")
    private Integer memoryLimit = 128;
}
