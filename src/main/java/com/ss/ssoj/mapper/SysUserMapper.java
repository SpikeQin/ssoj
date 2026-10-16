package com.ss.ssoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ss.ssoj.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 统计使用该用户名的账号数，包含已逻辑删除的账号。
     * 用户名上有唯一索引，逻辑删除的账号仍占用用户名，重复写入会触发数据库约束错误，
     * 因此注册与新建账号前需要连同已删除账号一起校验。
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    long countByUsernameIncludeDeleted(@Param("username") String username);
}
