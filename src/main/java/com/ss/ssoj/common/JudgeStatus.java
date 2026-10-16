package com.ss.ssoj.common;

/**
 * 判题状态码，与 submission.status 字段取值一一对应。
 */
public final class JudgeStatus {

    /** 待判题 */
    public static final int PENDING = 0;
    /** 判题中 */
    public static final int JUDGING = 1;
    /** 通过 */
    public static final int ACCEPTED = 2;
    /** 答案错误 */
    public static final int WRONG_ANSWER = 3;
    /** 运行超时 */
    public static final int TIME_LIMIT_EXCEEDED = 4;
    /** 运行错误 */
    public static final int RUNTIME_ERROR = 5;
    /** 编译错误 */
    public static final int COMPILE_ERROR = 6;
    /** 系统错误 */
    public static final int SYSTEM_ERROR = 7;

    private JudgeStatus() {
    }
}
