package com.ss.ssoj.service;

import com.ss.ssoj.common.PageResult;
import com.ss.ssoj.dto.SubmissionQuery;
import com.ss.ssoj.dto.SubmissionRequest;
import com.ss.ssoj.vo.SubmissionDetailVO;
import com.ss.ssoj.vo.SubmissionVO;

public interface SubmissionService {

    /** 提交代码并进入判题队列 */
    Long submit(SubmissionRequest request, Long userId);

    /** 提交记录分页查询 */
    PageResult<SubmissionVO> page(SubmissionQuery query, Long currentUserId, boolean admin);

    /** 提交详情，仅本人与管理员可查看 */
    SubmissionDetailVO detail(Long id, Long currentUserId, boolean admin);
}
