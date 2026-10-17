# 蚀山在线判题系统（SSoj）

一套可以本地部署的教学型在线判题系统：题库练习、在线评测、竞赛与榜单，外加一个完整的管理后台。
仓库自带 11 道题、124 个测试点、一场演示竞赛和一批演示提交记录，按下面的步骤导入后即可登录体验。

![题库](docs/screenshots/题库列表.png)

## 功能

- **题库**：按关键词、难度、做题状态筛选，Markdown 题面渲染，算法标签检索
- **在线评测**：支持 C / C++ / Java，提交后异步判题，逐用例返回状态与耗时，编译错误直接回显编译器信息
- **竞赛**：A / B / C 编号编排，按通过题数与罚时排名；可开启赛题隐藏，比赛结束后自动回到题库
- **榜单与统计**：总榜、个人统计、管理端运行概览
- **管理后台**：题目与测试用例、竞赛编排、用户与密码、公告与系统配置

| 题目详情与判题结果 | 竞赛榜单 |
| --- | --- |
| ![题目](docs/screenshots/题目详情与判题结果.png) | ![榜单](docs/screenshots/竞赛榜单.png) |

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | JDK 21、Spring Boot 4.1、MyBatis-Plus 3.5、Maven |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3、Vite、Element Plus、Vue Router、Axios |
| 鉴权 | JWT + 拦截器 + 权限注解 |
| 判题 | 固定线程池 + 有界队列，`ProcessBuilder` 调用 gcc / g++ / javac 编译运行 |

## 目录结构

```
├── src/main/java/com/ss/ssoj/
│   ├── auth/             登录拦截器、JWT 与权限注解
│   ├── common/           统一响应、异常处理与状态码
│   ├── config/           MyBatis-Plus 与判题参数配置
│   ├── controller/       接口层
│   ├── dto/ vo/          请求与响应模型
│   ├── entity/ mapper/   数据模型与持久层
│   ├── runner/           启动时恢复未完成的判题任务
│   └── service/          业务逻辑
├── src/main/resources/
│   ├── application.properties   数据源、判题语言与令牌配置
│   └── mapper/                  统计类自定义 SQL
├── sql/                  数据库脚本
├── web/                  前端工程（Vue 3）
└── docs/                 部署说明与界面截图
```

## 环境要求

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21 | 运行后端；Java 判题需要其中的 `javac`，只装 JRE 不行 |
| Maven | 3.8 及以上 | 构建后端 |
| MySQL | 8.0 | 业务数据存储 |
| Node.js | 18 及以上（推荐 20+） | 构建与运行前端 |
| MinGW-w64 | 8.1（x86_64、posix-seh） | C / C++ 判题需要 `gcc`、`g++` |

> 判题工具链**不需要加入系统 PATH**：只要把 `gcc` / `g++` / `javac` / `java` 所在的 `bin` 目录写进配置文件即可，见下一步。

## 本地运行

### 1. 建库并导入数据

进入 `sql` 目录，按顺序执行（脚本自带 `USE ssoj;`，会创建 `ssoj` 库）：

```bash
mysql -u root -p --default-character-set=utf8mb4 < 01_schema.sql
mysql -u root -p --default-character-set=utf8mb4 < 02_init_data.sql
mysql -u root -p --default-character-set=utf8mb4 < 03_problem_data.sql
```

| 脚本 | 内容 |
| --- | --- |
| `01_schema.sql` | 建表（8 张业务表），可重复执行 |
| `02_init_data.sql` | 账号、系统配置、公告 |
| `03_problem_data.sql` | 11 道题、124 个测试点、演示竞赛、演示提交记录 |
| `00_reset.sql` | 开发用：清空所有业务表后重建 |

> `02` 和 `03` 会先删除对应数据再写入，重复导入会覆盖现有账号 / 题目 / 提交记录。

> 演示竞赛的赛程固定为 2026-10-16 10:00 ~ 16:40（已结束），榜单按这个时间窗统计。想演示"比赛进行中"的赛题隐藏，在管理后台编辑竞赛、把起止时间改成当前时间即可。

### 2. 修改配置（必做）

打开 `src/main/resources/application.properties`，改两处：

**数据库连接**（改成你本机的账号密码）：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ssoj?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的密码
```

**判题工具链**（填三个 bin 目录，判题不会去读系统 PATH）：

```properties
judge.languages.c.bin-dir=D:/tools/mingw64/bin
judge.languages.cpp.bin-dir=D:/tools/mingw64/bin
judge.languages.java.bin-dir=C:/Program Files/Java/jdk-21/bin
```

- 路径用 `/` 分隔，带空格也没问题（JDK 默认就在 `Program Files` 下）
- MinGW 的目录是 `gcc.exe` 所在的那层 `bin`，不是上一级 `mingw64`
- 没配置或路径不对时，提交会判为 System Error，并在提示里写明"在哪个目录下找不到哪个程序"
- 更详细的排查见 [docs/判题环境配置.md](docs/判题环境配置.md)

### 3. 启动后端

```bash
mvn spring-boot:run
```

也可以用 IDEA 打开工程后直接运行 `SSojApplication`。启动成功后监听 `http://localhost:8080`，接口统一前缀 `/api`。

### 4. 启动前端

```bash
cd web
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`。前端已把 `/api` 代理到 `http://127.0.0.1:8080`，两个服务都启动后即可使用。

### 5. 登录

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `ztw666` | `123456` | 登录后可进入「管理后台」 |
| 学生 | 见 `02_init_data.sql` 中的学号 | `123456` | 可提交代码、参加竞赛 |

按课程演示的要求，密码在数据库中明文保存，请只在本地或内网使用，不要复用真实密码，也不要直接暴露到公网。

## 判题说明

- 支持语言：C（gcc）、C++（g++）、Java（javac / java）。语言、bin 目录与编译运行命令都集中在 `application.properties` 的 `judge.languages.*`，新增语言不需要改判题代码。
- 判题流程：提交入库后进入判题队列，依次完成编译、逐用例运行、结果比对与回写；页面每 700 ms 轮询提交详情展示进度。
- 资源限制：按题目时间限制计时，超时强制结束进程；单个用例输出超过 16 MB 判为运行错误；编译超时 20 秒。
- 结果比对：忽略每行行末空白与文末换行（兼容 CRLF），遇到首个未通过的用例即停止。
- 服务重启后，处于"待判题 / 判题中"的提交会在启动时自动重新入队。

## 常见问题

**端口被占用**：后端改 `application.properties` 的 `server.port`，前端改 `web/vite.config.js` 的 `server.port`，同时把代理目标改成后端实际端口。

**启动报数据库连接失败**：确认 MySQL 已启动、`ssoj` 库已导入、`application.properties` 里的账号密码与本机一致。

**提交后判题结果是 System Error**：多半是 `judge.languages.*.bin-dir` 没配或配错。系统错误信息里会写出具体目录和程序名，照着改即可。

**提交后页面一直显示 Pending**：说明提交请求没有成功（常见原因是 10 秒内的重复提交、题目已下线、判题队列已满）。打开浏览器开发者工具的 Network，查看 `submissions` 请求返回的 `message`。

**C++ 编译报 `bits/stdc++.h` 相关错误**：本机 MinGW 8.1 在 `-std=c++17` 下有头文件缺陷，项目已固定使用 `-std=gnu++14`，不要改成 c++17。

## 已知限制

- 判题在宿主机直接执行编译运行命令，没有容器沙箱，也不限制内存占用，仅适合教学与内网使用
- 不支持特殊判题（SPJ）与浮点误差比对，需要容差判定的题目要保证标准答案唯一
- 判题进度使用前端轮询实现，尚未接入 WebSocket 推送

## 说明

本项目为课程设计作品（小组：蚀山制造），需求规格说明书、操作说明与答辩 PPT 随课程设计一并提交。
