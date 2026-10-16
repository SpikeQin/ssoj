package com.ss.ssoj.vo;

import com.ss.ssoj.entity.Problem;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 题目视图对象：标签转换为数组，并补充通过率与当前用户的做题状态。
 */
@Data
public class ProblemVO {

    private Long id;
    private String title;
    private String description;
    private Integer difficulty;
    private List<String> tags = new ArrayList<>();
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer enabled;
    private Integer submitCount;
    private Integer acceptCount;
    private Integer acceptRate;
    /** 当前用户的做题状态：ac-已通过，wa-未通过，null-未尝试 */
    private String myStatus;
    /** 该题目当前所属的进行中竞赛编号，不在比赛中时为空 */
    private Long contestId;
    /** 进行中竞赛的名称，用于题库与题目页提示 */
    private String contestTitle;

    public static ProblemVO from(Problem problem) {
        ProblemVO vo = new ProblemVO();
        vo.setId(problem.getId());
        vo.setTitle(problem.getTitle());
        vo.setDescription(problem.getDescription());
        vo.setDifficulty(problem.getDifficulty());
        vo.setTags(splitTags(problem.getTags()));
        vo.setTimeLimit(problem.getTimeLimit());
        vo.setMemoryLimit(problem.getMemoryLimit());
        vo.setEnabled(problem.getEnabled());
        vo.setSubmitCount(problem.getSubmitCount());
        vo.setAcceptCount(problem.getAcceptCount());
        vo.setAcceptRate(calculateRate(problem.getSubmitCount(), problem.getAcceptCount()));
        return vo;
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private static int calculateRate(Integer submitCount, Integer acceptCount) {
        if (submitCount == null || submitCount == 0 || acceptCount == null) {
            return 0;
        }
        return Math.round(acceptCount * 100f / submitCount);
    }
}
