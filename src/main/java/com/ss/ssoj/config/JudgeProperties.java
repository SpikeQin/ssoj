package com.ss.ssoj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 判题参数：工作目录、线程数、输出上限与各语言的编译运行命令。
 * 语言命令写在 application.properties 的 judge.languages.* 下，新增语言不需要改判题逻辑。
 */
@Data
@Component
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {

    /** 判题工作目录，每个提交在其中创建独立子目录 */
    private String workspace = "./judge-workspace";

    /** 同时执行的判题任务数 */
    private int threads = 2;

    /** 等待队列长度，超过后拒绝新提交 */
    private int queueSize = 100;

    /** 程序输出上限，单位 KB */
    private int outputLimitKb = 16384;

    /** 编译超时，单位秒 */
    private int compileTimeoutSeconds = 20;

    private Map<String, Language> languages = new LinkedHashMap<>();

    @Data
    public static class Language {

        /** 前端下拉框显示的名称 */
        private String label;

        /** 源文件名，如 main.cpp */
        private String sourceFile;

        /** 编译命令，为空表示解释型语言无需编译 */
        private String compileCommand;

        /** 运行命令，如 main 或 java -cp . Main */
        private String runCommand;

        /**
         * 该语言工具链的 bin 目录，例如 C:/mingw64/bin 或 C:/Program Files/Java/jdk-21/bin。
         * 填写后判题会在该目录下查找 gcc / g++ / javac / java，不依赖系统 PATH；
         * 留空或目录下找不到时回退到系统 PATH。
         */
        private String binDir;
    }
}
