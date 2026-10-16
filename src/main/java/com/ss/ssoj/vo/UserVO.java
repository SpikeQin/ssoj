package com.ss.ssoj.vo;

import com.ss.ssoj.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图对象：只暴露前端需要的字段，不包含密码密文。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;

    public static UserVO from(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
