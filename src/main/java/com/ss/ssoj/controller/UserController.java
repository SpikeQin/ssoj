package com.ss.ssoj.controller;

import com.ss.ssoj.auth.UserContext;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.dto.ChangePasswordRequest;
import com.ss.ssoj.dto.ProfileRequest;
import com.ss.ssoj.service.RankService;
import com.ss.ssoj.service.UserService;
import com.ss.ssoj.vo.PersonalStatsVO;
import com.ss.ssoj.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 个人中心接口，需要登录。 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final RankService rankService;

    public UserController(UserService userService, RankService rankService) {
        this.userService = userService;
        this.rankService = rankService;
    }

    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.success(userService.getProfile(UserContext.userId()));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileRequest request) {
        return Result.success(userService.updateProfile(UserContext.userId(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(UserContext.userId(), request);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<PersonalStatsVO> statistics() {
        return Result.success(rankService.personalStats(UserContext.userId()));
    }
}
