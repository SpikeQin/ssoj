# 蚀山在线判题系统 前端

技术栈：Vue 3 + Vite + Element Plus + Vue Router + Axios

## 运行

```bash
npm install
npm run dev
```

开发地址：http://localhost:5173

`vite.config.js` 已把 `/api` 代理到后端 `http://127.0.0.1:8080`，前端与后端分别启动即可联调。生产构建使用：

```bash
npm run build      # 产物输出到 dist/
```

## 目录结构

```
src/
├── api/
│   ├── request.js      Axios 实例：统一前缀、令牌注入、401 处理
│   └── index.js        业务接口：全部对接后端 REST 接口
├── components/
│   ├── AnnouncementBoard.vue 题库页顶部的公告区
│   ├── CodeEditor.vue   代码编辑区：行号、等宽字体、Tab 缩进
│   ├── JudgeStatus.vue  判题状态：统一文案与配色
│   ├── MarkdownView.vue 题面与公告共用的 Markdown 渲染
│   └── DifficultyTag.vue 难度标签
├── router/index.js     路由表与登录/管理员守卫
├── store/user.js       登录状态（令牌与用户信息）
├── styles/main.css     设计变量与全局样式，判题状态配色集中在此
├── utils/
│   ├── dict.js         判题状态、难度字典
│   ├── format.js       日期时间格式化
│   └── language.js     判题语言与默认代码模板
├── views/
│   ├── ProblemList.vue      题库列表：筛选、分页
│   ├── ProblemDetail.vue    题目详情：题面、提交、判题结果、我的提交
│   ├── SubmissionList.vue   提交记录：范围与状态筛选
│   ├── SubmissionDetail.vue 提交详情：代码、用例结果、编译信息
│   ├── ContestList.vue      竞赛列表
│   ├── ContestDetail.vue    竞赛详情：题目清单与竞赛榜单
│   ├── RankList.vue         总榜
│   ├── Profile.vue          个人中心：资料、密码、解题统计
│   ├── Login.vue            登录与注册
│   └── admin/               管理后台
│       ├── AdminLayout.vue    侧边导航
│       ├── Dashboard.vue      管理首页：概览数据与最近提交
│       ├── ProblemManage.vue  题目管理：新建、编辑、测试用例、上下线
│       ├── ContestManage.vue  竞赛管理：创建竞赛与题目编排
│       ├── UserManage.vue     用户管理：启用禁用、重置密码
│       └── SystemSetting.vue  公告与系统配置
└── App.vue             顶部导航与整体布局
```

## 判题进度说明

后端目前没有推送通道，`api/index.js` 中的 `submitCode` 通过轮询提交详情获取判题状态，
并在完成后把逐用例结果回放成事件推给页面。后端后续接入 WebSocket 时，只需要替换这个函数，
页面组件不需要改动。

