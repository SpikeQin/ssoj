package com.ss.ssoj.vo;

import com.ss.ssoj.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台用户列表项：在基础资料上补充提交与通过数量。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserAdminVO extends UserVO {

    private Integer submitCount;
    private Integer acceptCount;

    public static UserAdminVO from(SysUser user, int submitCount, int acceptCount) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setSubmitCount(submitCount);
        vo.setAcceptCount(acceptCount);
        return vo;
    }
}
