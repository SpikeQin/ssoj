package com.ss.ssoj.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProblemCaseRequest {

    @NotBlank(message = "用例输入不能为空")
    private String input;

    @NotBlank(message = "用例输出不能为空")
    private String output;

    private Integer enabled = 1;
}
