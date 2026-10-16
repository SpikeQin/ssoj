package com.ss.ssoj.vo;

import lombok.Data;

/**
 * 总榜的一行。solved 与 submitCount 由自定义 SQL 聚合，通过率在服务层计算。
 */
@Data
public class RankRowVO {

    private Integer rank;
    private Long userId;
    private String nickname;
    private Integer solved;
    private Integer submitCount;
    private Integer acceptCount;
    private Integer acceptRate;
}
