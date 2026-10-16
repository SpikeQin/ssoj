package com.ss.ssoj.controller;

import com.ss.ssoj.auth.Public;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.dto.LoginRequest;
import com.ss.ssoj.dto.RegisterRequest;
import com.ss.ssoj.service.UserService;
import com.ss.ssoj.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录与注册接口，无需登录即可访问。 */
@Public
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }
}
