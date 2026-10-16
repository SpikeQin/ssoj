/*
 * ============================================================================
 * 蚀山在线判题系统 —— 重建脚本（危险操作）
 *
 * 说明：
 *   本脚本会删除 ssoj 库中的全部业务表与数据，仅用于开发阶段重建表结构。
 *   执行顺序：00_reset.sql -> 01_schema.sql -> 02_init_data.sql
 *
 *   注意：删除顺序与外键依赖相反，先删子表再删父表。
 * ============================================================================
 */

CREATE DATABASE IF NOT EXISTS `ssoj`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `ssoj`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_config`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `submission`;
DROP TABLE IF EXISTS `contest_problem`;
DROP TABLE IF EXISTS `contest`;
DROP TABLE IF EXISTS `problem_case`;
DROP TABLE IF EXISTS `problem`;
DROP TABLE IF EXISTS `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;
