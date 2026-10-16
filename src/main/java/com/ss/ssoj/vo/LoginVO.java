package com.ss.ssoj.vo;

import lombok.Data;

/**
 * 登录成功返回：令牌与用户信息。
 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}
