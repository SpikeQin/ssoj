package com.ss.ssoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ss.ssoj.entity.Submission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
}
