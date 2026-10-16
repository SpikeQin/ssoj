package com.ss.ssoj.service;

public interface JudgeService {

    /** 把提交放入判题队列异步执行 */
    void submit(Long submissionId);

    /** 服务启动时把未完成的提交重新排队 */
    void requeueUnfinished();

    /** 清理判题工作目录中的历史残留目录 */
    void cleanWorkspace();

    /** 判题等待队列是否已满 */
    boolean isQueueFull();
}
