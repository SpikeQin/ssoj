package com.ss.ssoj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {

    @NotNull(message = "缺少题目编号")
    private Long problemId;

    @NotBlank(message = "请选择判题语言")
    private String language;

    @NotBlank(message = "代码不能为空")
    private String code;

    /** 竞赛内提交时前端带入的竞赛编号，仅用于界面提示；实际归属由服务端按竞赛时间判定 */
    private Long contestId;
}
