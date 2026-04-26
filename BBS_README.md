# 企业内部论坛系统

基于RuoYi-Vue框架实现的企业内部论坛平台，参考华为心声论坛的功能设计。

## 功能特性

### 核心功能
1. **帖子管理**
   - 发布、编辑、删除帖子
   - 帖子分类管理
   - 帖子置顶、精华标记
   - 帖子浏览、点赞、收藏统计

2. **评论系统**
   - 多级评论回复
   - 评论点赞功能
   - 评论管理

3. **用户互动**
   - 点赞功能（帖子、评论）
   - 收藏功能
   - 关注功能
   - 用户个人中心

4. **分类标签**
   - 论坛分类管理
   - 标签系统
   - 热门标签展示

5. **通知系统**
   - 评论通知
   - 回复通知
   - 点赞通知
   - 关注通知

## 技术栈

### 后端
- Spring Boot 2.5.15
- MyBatis
- MySQL
- Spring Security
- JWT

### 前端
- Vue 2.6.12
- Element UI 2.15.14
- Axios
- Vue Router

## 数据库表结构

系统包含以下核心表：
- `bbs_category` - 论坛分类表
- `bbs_post` - 帖子表
- `bbs_comment` - 评论表
- `bbs_like` - 点赞表
- `bbs_collect` - 收藏表
- `bbs_tag` - 标签表
- `bbs_post_tag` - 帖子标签关联表
- `bbs_follow` - 关注表
- `bbs_notification` - 通知表

## 安装部署

### 1. 数据库初始化

执行SQL脚本创建数据库表：
```sql
-- 执行 sql/bbs_forum.sql
source sql/bbs_forum.sql;
```

### 2. 后端配置

1. 修改数据库连接配置：`ruoyi-admin/src/main/resources/application.yml`
2. 启动后端服务：`RuoYiApplication.java`

### 3. 前端配置

1. 安装依赖：
```bash
cd ruoyi-ui
npm install
```

2. 启动开发服务器：
```bash
npm run dev
```

3. 构建生产版本：
```bash
npm run build:prod
```

## 使用说明

### 访问论坛

1. 登录系统后，在左侧菜单找到"企业论坛"
2. 或直接访问：`/bbs/index`

### 发布帖子

1. 点击"发布帖子"按钮
2. 选择分类、填写标题和内容
3. 点击"确定"发布

### 浏览帖子

1. 在论坛首页浏览帖子列表
2. 可以按分类筛选
3. 可以按最新、热门、精华排序
4. 点击帖子标题查看详情

### 评论互动

1. 在帖子详情页可以发表评论
2. 可以对评论进行回复（多级回复）
3. 可以点赞帖子和评论
4. 可以收藏帖子

## API接口

### 帖子接口
- `GET /bbs/post/list` - 获取帖子列表
- `GET /bbs/post/{postId}` - 获取帖子详情
- `POST /bbs/post` - 发布帖子
- `PUT /bbs/post` - 修改帖子
- `DELETE /bbs/post/{postIds}` - 删除帖子
- `POST /bbs/post/like/{postId}` - 点赞/取消点赞
- `POST /bbs/post/collect/{postId}` - 收藏/取消收藏

### 分类接口
- `GET /bbs/category/list` - 获取分类列表
- `GET /bbs/category/{categoryId}` - 获取分类详情
- `POST /bbs/category` - 新增分类
- `PUT /bbs/category` - 修改分类
- `DELETE /bbs/category/{categoryIds}` - 删除分类

### 评论接口
- `GET /bbs/comment/list` - 获取评论列表
- `POST /bbs/comment` - 发表评论
- `PUT /bbs/comment` - 修改评论
- `DELETE /bbs/comment/{commentIds}` - 删除评论
- `POST /bbs/comment/like/{commentId}` - 点赞/取消点赞评论

## 文件结构

```
BBS/
├── sql/
│   └── bbs_forum.sql          # 数据库表结构
├── ruoyi-system/
│   └── src/main/java/com/ruoyi/system/
│       ├── domain/            # 实体类
│       ├── mapper/            # Mapper接口
│       └── service/           # Service接口和实现
├── ruoyi-admin/
│   └── src/main/java/com/ruoyi/web/controller/bbs/
│       └── *.java             # Controller控制器
└── ruoyi-ui/
    └── src/
        ├── api/bbs/           # API接口
        └── views/bbs/         # 前端页面
```

## 注意事项

1. 确保数据库已正确配置并执行了SQL脚本
2. 确保后端服务正常运行
3. 前端需要配置正确的API基础地址
4. 用户需要登录后才能发布帖子和评论
5. 管理员可以在系统管理中添加论坛分类

## 扩展功能建议

1. 添加图片上传功能
2. 添加@用户功能
3. 添加私信功能
4. 添加搜索功能
5. 添加数据统计功能
6. 添加移动端适配

## 技术支持

如有问题，请参考RuoYi官方文档：http://doc.ruoyi.vip
