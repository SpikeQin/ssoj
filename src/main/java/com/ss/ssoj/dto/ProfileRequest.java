package com.ss.ssoj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {

    @NotBlank(message = "请输入昵称")
    @Size(max = 32, message = "昵称不能超过 32 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;
}
