package com.ss.ssoj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员新建账号的请求参数。 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "请输入用户名")
    @Size(min = 4, max = 16, message = "用户名长度为 4 到 16 位")
    private String username;

    @NotBlank(message = "请输入昵称")
    @Size(max = 32, message = "昵称不能超过 32 个字符")
    private String nickname;

    @NotBlank(message = "请输入初始密码")
    @Size(min = 6, max = 20, message = "密码长度为 6 到 20 位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    /** 角色：0-学生，1-管理员，默认学生 */
    private Integer role = 0;
}
