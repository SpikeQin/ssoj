package com.ss.ssoj.runner;

import com.ss.ssoj.service.JudgeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 启动时把数据库中未完成的提交重新放入判题队列，避免重启后一直停留在待判题状态。 */
@Component
public class JudgeBootstrap implements ApplicationRunner {

    private final JudgeService judgeService;

    public JudgeBootstrap(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        judgeService.cleanWorkspace();
        judgeService.requeueUnfinished();
    }
}
