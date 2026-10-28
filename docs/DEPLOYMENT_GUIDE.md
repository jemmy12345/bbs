# Smart BBS 部署运行说明

## 1. 环境要求


| 组件      | 建议版本      | 说明                    |
| ------- | --------- | --------------------- |
| JDK     | Java 17   | 后端运行建议使用 Java 17      |
| Maven   | 3.6+      | 用于后端编译打包              |
| Node.js | 16+       | 用于前端安装依赖和构建           |
| MySQL   | 5.7+/8.0+ | 默认数据库名为 `bbsdb`       |
| Redis   | 5.0+      | 默认地址 `localhost:6379` |


## 2. 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS bbsdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

1. 导入最新版完整数据库备份：

```powershell
mysql -uroot -p123456 < sql/bbsdb_final_2026-05-24.sql
```

`bbsdb_final_2026-05-24.sql` 是当前验收版本导出的完整备份，已包含最新表结构、菜单权限、演示账号、提案评审配置和基础演示数据。首次部署时优先恢复该完整备份。

`sql` 目录中的其他脚本主要用于开发维护或增量升级；只有在没有完整备份、需要从空库逐步升级时才需要单独使用。

1. 默认数据库连接配置位于：

```text
backend/bbs-admin/src/main/resources/application-dev.yml
```

默认配置：

```text
数据库: bbsdb
账号: root
密码: 123456
Redis: localhost:6379
```

## 3. 后端启动

在项目根目录执行：

```powershell
mvn -pl backend/bbs-admin -am clean package -DskipTests
```

打包完成后，运行：

```powershell
java -jar backend/bbs-admin/target/bbs-admin.jar
```

后端默认端口：

```text
http://localhost:9082
```

如果使用本机已打好的运行包，也可以运行：

```powershell
java -jar deploy/bbs-admin.jar
```

## 4. 前端启动

进入前端目录：

```powershell
cd frontend
npm install
npm run dev -- --port 80
```

前端访问地址：

```text
http://localhost/bbs/
```

生产构建命令：

```powershell
npm run build:prod
```

构建结果输出到：

```text
frontend/dist
```

## 5. 默认演示账号

所有演示账号默认密码为：

```text
123456
```


| 账号         | 用途        |
| ---------- | --------- |
| admin      | 系统管理与后台配置 |
| zhangsan   | 普通用户演示    |
| l1reviewer | 提案初审演示    |
| l2reviewer | 提案复审演示    |
| l3reviewer | 提案终审演示    |


## 6. 功能验证

提供了全量接口验证脚本：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/full-system-test.ps1 -NoStart -KeepRunning
```

脚本会覆盖：

- 登录与权限信息
- 后台管理接口
- 论坛帖子与评论接口
- 内容风控预检
- 提案提交、初审、复审、终审流程
- 提案评审配置
- 账号详情与字符串用户 ID 场景
- 评论发布与本人删除

测试报告输出目录：

```text
runtime/test-reports
```

## 7. 常见问题

### 后端启动失败

检查 MySQL、Redis 是否已启动，并确认 `application-dev.yml` 中数据库账号密码正确。

### 前端无法访问后端接口

检查后端端口 `9082` 是否监听，并确认前端代理配置未被修改。

### AI 相关功能连接异常

优先确认后端是否使用 Java 17 运行，并检查外部接口配置是否完整。

### 提案评审无法推进

进入后台的提案评审配置，确认初审、复审、终审均已指定对应处理人。