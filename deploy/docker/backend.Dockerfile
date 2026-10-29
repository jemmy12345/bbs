# Smart BBS 后端镜像：Maven(JDK 8) 编译打包 -> JRE 8 运行
# 构建上下文为项目根目录（smart-bbs/）

FROM maven:3.8-eclipse-temurin-8 AS build
ARG CN_MIRROR=0
WORKDIR /src
# 国内网络可启用阿里云 Maven 镜像
RUN if [ "$CN_MIRROR" = "1" ]; then \
      mkdir -p /root/.m2 && printf '%s\n' \
      '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf>' \
      '<url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' \
      > /root/.m2/settings.xml; \
    fi
COPY pom.xml ./
COPY backend ./backend
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -q -B -pl backend/bbs-admin -am -DskipTests package \
 && cp backend/bbs-admin/target/bbs-admin.jar /app.jar

FROM eclipse-temurin:8-jre
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY --from=build /app.jar /app/app.jar
RUN mkdir -p /app/upload /tmp/smart-bbs-logs
EXPOSE 9082
ENTRYPOINT ["java", "-Xms256m", "-Xmx1024m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
