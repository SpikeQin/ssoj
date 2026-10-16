package com.ss.ssoj.controller;

import com.ss.ssoj.auth.Public;
import com.ss.ssoj.common.Result;
import com.ss.ssoj.service.RankService;
import com.ss.ssoj.vo.RankRowVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 榜单接口：总榜对游客开放。 */
@Public
@RestController
@RequestMapping("/api/ranks")
public class RankController {

    private final RankService rankService;

    public RankController(RankService rankService) {
        this.rankService = rankService;
    }

    @GetMapping("/global")
    public Result<List<RankRowVO>> global(@RequestParam(defaultValue = "100") int limit) {
        return Result.success(rankService.globalRank(limit));
    }
}
