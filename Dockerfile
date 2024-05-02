# 使用官方的Java运行环境作为基础镜像
FROM openjdk:8-jdk-alpine

WORKDIR /app

# 将jar文件复制到容器内的/app目录下
COPY target/codesanbox-0.0.1-SNAPSHOT.jar /app/app.jar


ARG DOCKER_HOST

ENV DOCKER_HOST=$DOCKER_HOST

# 设置容器启动时执行的命令
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod","-c"]
