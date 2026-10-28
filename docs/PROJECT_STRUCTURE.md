# 项目目录说明

## 顶层目录

```text
smart-bbs/
  backend/   后端工程
  frontend/  前端工程
  sql/       数据库脚本
  scripts/   本地脚本
  docs/      项目文档
  deploy/    本机运行包
  runtime/   本机运行数据
```

## 后端目录

`backend/bbs-admin` 是后端启动入口，包含接口控制器和应用配置。

`backend/bbs-system` 是业务核心模块，包含论坛、评论、提案、评审、风控、敏感词库等业务逻辑。

`backend/bbs-framework` 是基础框架模块，包含登录认证、权限校验、安全过滤、全局异常和 Web 配置。

`backend/bbs-common` 是公共模块，包含通用工具类、公共返回对象、Redis 工具、Excel 工具和基础注解。

`backend/bbs-quartz` 是定时任务模块，用于后台周期性任务能力。

`backend/bbs-generator` 是代码生成辅助模块，主要用于开发阶段快速生成基础代码。

## 前端目录

`frontend` 是 Vue 前端工程，包含论坛前台页面、提案页面、后台管理页面和通用组件。

## 数据与脚本目录

`sql` 存放建表、升级和演示账号脚本；历史数据库备份归档在论文资料目录，避免和交付源码混在一起。

`scripts` 存放本地启动、打包、验证脚本。

`deploy` 存放本机运行 jar。

`runtime` 存放上传文件等运行期数据，不作为核心源码。
