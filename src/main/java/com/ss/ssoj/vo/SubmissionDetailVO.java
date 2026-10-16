package com.ss.ssoj.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 提交详情：在列表字段基础上补充源代码、用例明细与错误信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmissionDetailVO extends SubmissionVO {

    private String code;

    private String errorMessage;

    private List<CaseResultVO> cases = new ArrayList<>();
}
