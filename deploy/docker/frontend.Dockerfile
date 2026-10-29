# Smart BBS 前端镜像：Node 16 打包 -> Nginx 托管静态页面并反向代理后端接口
# 构建上下文为项目根目录（smart-bbs/）

FROM node:16-bullseye-slim AS build
ARG CN_MIRROR=0
WORKDIR /src
RUN if [ "$CN_MIRROR" = "1" ]; then npm config set registry https://registry.npmmirror.com; fi
# package-lock.json 被 frontend/.gitignore 排除，从仓库拉取的代码里可能没有，这里两种情况都兼容
COPY frontend/package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    if [ -f package-lock.json ]; then npm ci --no-audit --no-fund; \
    else npm install --no-audit --no-fund; fi
COPY frontend ./
RUN npm run build:prod

FROM nginx:1.25-alpine
ENV TZ=Asia/Shanghai
# 前端 publicPath 为 /bbs，静态文件放到 html/bbs 下
COPY --from=build /src/dist /usr/share/nginx/html/bbs
COPY deploy/docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
