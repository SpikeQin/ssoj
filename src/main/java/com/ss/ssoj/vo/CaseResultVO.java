package com.ss.ssoj.vo;

import lombok.Data;

/**
 * 单个测试用例的判题结果，最终以 JSON 数组保存在 submission.case_detail 中。
 */
@Data
public class CaseResultVO {

    /** 用例编号，按判题执行顺序从 1 开始，便于前端展示「用例 1、2、3」 */
    private Long caseId;

    /** 判题状态码：2-通过，3-答案错误，4-超时，5-运行错误 */
    private Integer status;

    /** 该用例耗时，单位毫秒 */
    private Integer timeUsed;

    private String message;

    public CaseResultVO() {
    }

    public CaseResultVO(Long caseId, Integer status, Integer timeUsed, String message) {
        this.caseId = caseId;
        this.status = status;
        this.timeUsed = timeUsed;
        this.message = message;
    }
}
