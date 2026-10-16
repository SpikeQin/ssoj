package com.ss.ssoj.vo;

import lombok.Data;

/**
 * 竞赛榜单中某个参赛者某道题的作答情况。
 */
@Data
public class ContestCellVO {

    /** 是否已通过 */
    private boolean accepted;

    /** 提交次数，用于榜单展示 +2、-1 这类标记 */
    private int tries;
}
