# Smart BBS

智慧论坛与提案管理系统，面向企业内部沟通、意见反馈、匿名表达、内容风控、提案评审和成果公示场景。

## 目录结构

```text
smart-bbs/
  backend/       后端 Spring Boot 多模块工程
  frontend/      前端 Vue 工程
  sql/           数据库结构、初始化与升级脚本
  scripts/       本地构建、启动和验证脚本
  docs/          项目说明与部署文档
  deploy/        本机运行包目录
  runtime/       本机上传文件等运行数据
```

## 后端模块

```text
backend/
  bbs-admin      后端启动入口与接口控制器
  bbs-system     论坛、评论、提案、风控等核心业务
  bbs-framework  登录认证、权限、安全过滤和框架配置
  bbs-common     通用工具、基础对象和公共能力
  bbs-quartz     定时任务能力
  bbs-generator  代码生成辅助模块
```

## 常用命令

```powershell
# 后端编译
mvn -pl backend/bbs-admin -am -DskipTests compile

# 后端打包
mvn -pl backend/bbs-admin -am clean package -DskipTests

# 前端开发运行
cd frontend
npm run dev -- --port 80

# 前端生产打包
cd frontend
npm run build:prod
```

## 默认访问

- 前台地址：`http://localhost/bbs/`
- 后端端口：`9082`
- 数据库：`bbsdb`
