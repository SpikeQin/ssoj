package com.ss.ssoj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "请输入用户名")
    @Size(min = 4, max = 16, message = "用户名长度为 4 到 16 位")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 20, message = "密码长度为 6 到 20 位")
    private String password;

    private String nickname;

    private String email;
}
