/*
 * ============================================================================
 * 蚀山在线判题系统 —— 数据库结构脚本
 * 项目名称：在线判题系统（SSoj）
 * 数据库：MySQL 8.0
 * 字符集：utf8mb4 / utf8mb4_general_ci
 *
 * 说明：
 *   1. 脚本可重复执行，已存在的库与表不会被重建，也不会清空已有数据。
 *   2. 需要重建表结构时，请先执行 00_reset.sql（该脚本会删除数据）。
 *   3. 设计依据：《在线判题系统需求规格说明书》第 5 章数据需求。
 * ============================================================================
 */

CREATE DATABASE IF NOT EXISTS `ssoj`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `ssoj`;

/* ---------------------------------------------------------------------------
 * 1. 用户表
 *    角色与账号状态使用数值枚举；登录失败次数与锁定时间用于实现 5 次失败锁 10 分钟。
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`         VARCHAR(32)  NOT NULL COMMENT '登录用户名，全局唯一',
    `password`         VARCHAR(100) NOT NULL COMMENT '明文保存，生产环境应改为 BCrypt',
    `nickname`         VARCHAR(32)  NOT NULL COMMENT '昵称，列表与榜单展示使用',
    `email`            VARCHAR(64)           DEFAULT NULL COMMENT '邮箱',
    `role`             TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-学生，1-管理员',
    `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    `login_fail_count` TINYINT      NOT NULL DEFAULT 0 COMMENT '连续登录失败次数，登录成功后清零',
    `lock_until`       DATETIME              DEFAULT NULL COMMENT '账号锁定截止时间，为空表示未锁定',
    `last_login_time`  DATETIME              DEFAULT NULL COMMENT '最近一次登录时间',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    KEY `idx_sys_user_role` (`role`, `deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户表';

/* ---------------------------------------------------------------------------
 * 2. 题目表
 *    主键即题目编号；submit_count / accept_count 为列表展示用的冗余统计字段，
 *    在判题完成时更新，避免列表页每行都做聚合查询。
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `problem`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID，对外作为题目编号',
    `title`        VARCHAR(128) NOT NULL COMMENT '题目标题',
    `description`  LONGTEXT     NOT NULL COMMENT '题面，Markdown 文本',
    `difficulty`   TINYINT      NOT NULL DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
    `tags`         VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标签，多个标签用英文逗号分隔',
    `time_limit`   INT          NOT NULL DEFAULT 1000 COMMENT '时间限制，单位毫秒',
    `memory_limit` INT          NOT NULL DEFAULT 128 COMMENT '内存限制，单位 MB',
    `enabled`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-已下线，1-已上线',
    `submit_count` INT          NOT NULL DEFAULT 0 COMMENT '提交次数，冗余统计字段',
    `accept_count` INT          NOT NULL DEFAULT 0 COMMENT '通过次数，冗余统计字段',
    `create_by`    BIGINT                DEFAULT NULL COMMENT '创建人ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_problem_list` (`enabled`, `deleted`),
    KEY `idx_problem_difficulty` (`difficulty`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '题目表';

/* ---------------------------------------------------------------------------
 * 3. 测试用例表
 *    与题目为多对多之外的从属关系，题目删除时级联删除用例。
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `problem_case`
(
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `problem_id`  BIGINT   NOT NULL COMMENT '所属题目ID',
    `input`       LONGTEXT          DEFAULT NULL COMMENT '测试用例输入数据',
    `output`      LONGTEXT          DEFAULT NULL COMMENT '测试用例标准输出',
    `enabled`     TINYINT  NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `sort_order`  INT      NOT NULL DEFAULT 0 COMMENT '执行顺序，序号小的先执行',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_problem_case_problem` (`problem_id`, `enabled`, `sort_order`),
    CONSTRAINT `fk_problem_case_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '测试用例表';

/* ---------------------------------------------------------------------------
 * 4. 竞赛表
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `contest`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(128) NOT NULL COMMENT '竞赛标题',
    `description` LONGTEXT              DEFAULT NULL COMMENT '竞赛说明，Markdown 文本',
    `start_time`  DATETIME     NOT NULL COMMENT '开始时间',
    `end_time`    DATETIME     NOT NULL COMMENT '结束时间',
    `hide_problems` TINYINT    NOT NULL DEFAULT 1 COMMENT '竞赛未结束时是否在学生题库中隐藏赛题：0-否，1-是',
    `create_by`   BIGINT                DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_contest_time` (`start_time`, `end_time`),
    KEY `idx_contest_deleted` (`deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '竞赛表';

/* ---------------------------------------------------------------------------
 * 5. 竞赛题目表
 *    记录竞赛内题目的顺序与显示编号，显示编号在竞赛内唯一。
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `contest_problem`
(
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `contest_id`    BIGINT      NOT NULL COMMENT '竞赛ID',
    `problem_id`    BIGINT      NOT NULL COMMENT '题目ID',
    `display_label` VARCHAR(4)  NOT NULL COMMENT '竞赛内的显示编号，如 A、B、C',
    `sort_order`    INT         NOT NULL DEFAULT 0 COMMENT '排序序号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_contest_problem_label` (`contest_id`, `display_label`),
    UNIQUE KEY `uk_contest_problem` (`contest_id`, `problem_id`),
    KEY `idx_contest_problem_problem` (`problem_id`),
    CONSTRAINT `fk_contest_problem_contest` FOREIGN KEY (`contest_id`) REFERENCES `contest` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_contest_problem_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '竞赛题目表';

/* ---------------------------------------------------------------------------
 * 6. 提交记录表
 *    contest_id 为空表示题库练习提交，避免使用 0 作为特殊值。
 *    case_detail 保存各用例判题结果的 JSON 数组文本，格式示例：
 *    [{"caseId":1,"status":2,"timeUsed":12,"memoryUsed":1024,"message":"通过"}]
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `submission`
(
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT      NOT NULL COMMENT '提交用户ID',
    `problem_id`    BIGINT      NOT NULL COMMENT '题目ID',
    `contest_id`    BIGINT               DEFAULT NULL COMMENT '所属竞赛ID，为空表示题库练习提交',
    `language`      VARCHAR(16) NOT NULL COMMENT '判题语言：c、cpp、java',
    `code`          LONGTEXT    NOT NULL COMMENT '提交的源代码',
    `status`        TINYINT     NOT NULL DEFAULT 0 COMMENT '判题状态：0-待判题，1-判题中，2-通过，3-答案错误，4-超时，5-运行错误，6-编译错误，7-系统错误',
    `time_used`     INT                  DEFAULT NULL COMMENT '最大运行耗时，单位毫秒',
    `error_message` TEXT                 DEFAULT NULL COMMENT '编译错误信息或系统错误信息',
    `case_detail`   LONGTEXT             DEFAULT NULL COMMENT '各测试用例判题结果，JSON 数组文本',
    `submit_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `judge_time`    DATETIME             DEFAULT NULL COMMENT '判题完成时间',
    PRIMARY KEY (`id`),
    KEY `idx_submission_user_time` (`user_id`, `submit_time`),
    KEY `idx_submission_problem_status` (`problem_id`, `status`),
    KEY `idx_submission_status` (`status`),
    KEY `idx_submission_contest` (`contest_id`),
    CONSTRAINT `fk_submission_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_submission_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
    CONSTRAINT `fk_submission_contest` FOREIGN KEY (`contest_id`) REFERENCES `contest` (`id`)
        ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '提交记录表';

/* ---------------------------------------------------------------------------
 * 7. 公告表
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `announcement`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(128) NOT NULL COMMENT '公告标题',
    `content`     LONGTEXT     NOT NULL COMMENT '公告正文，Markdown 文本',
    `create_by`   BIGINT                DEFAULT NULL COMMENT '发布人ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_announcement_time` (`deleted`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '公告表';

/* ---------------------------------------------------------------------------
 * 8. 系统配置表
 *    键值对形式保存站点与判题相关参数，应用启动后读取，修改后刷新页面生效。
 * ------------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `sys_config`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`   VARCHAR(64)  NOT NULL COMMENT '配置项键名',
    `config_value` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '配置项值',
    `remark`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '配置项说明',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_config_key` (`config_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统配置表';

