package com.ss.ssoj.service;

import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.dto.ProblemCaseRequest;
import com.ss.ssoj.dto.ProblemQuery;
import com.ss.ssoj.dto.ProblemSaveRequest;
import com.ss.ssoj.entity.ProblemCase;
import com.ss.ssoj.vo.ProblemVO;

import java.util.List;

public interface ProblemService {

    /** 题目分页检索，管理员可额外看到已下线的题目 */
    PageResult<ProblemVO> page(ProblemQuery query, Long userId, boolean admin);

    /** 题目详情 */
    ProblemVO detail(Long id, Long userId, boolean admin);

    /** 新增或更新题目 */
    Long save(ProblemSaveRequest request, Long operatorId);

    /** 题目上线或下线 */
    void setEnabled(Long id, Integer enabled);

    /** 查询题目的测试用例，仅管理员可见 */
    List<ProblemCase> listCases(Long problemId);

    /** 覆盖保存题目的测试用例 */
    void saveCases(Long problemId, List<ProblemCaseRequest> cases);

    /** 删除题目（逻辑删除，历史提交保留） */
    void delete(Long id);
}
