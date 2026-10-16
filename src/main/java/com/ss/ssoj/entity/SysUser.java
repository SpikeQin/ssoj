package com.ss.ssoj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表：账号、角色与资料信息。密码按课程演示要求明文保存，不对公网开放。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 明文密码，接口返回统一使用 UserVO，实体不直接暴露给前端 */
    private String password;

    private String nickname;

    private String email;

    /** 角色：0-学生，1-管理员 */
    private Integer role;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    private Integer loginFailCount;

    private LocalDateTime lockUntil;

    /** 最近一次登录成功时间，登录时更新 */
    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
