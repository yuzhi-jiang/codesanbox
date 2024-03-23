package com.anml.codesanbox;

import cn.hutool.json.JSONUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CodesanboxApplication {

    public static void main(String[] args) {
        connectDocker();
        SpringApplication.run(CodesanboxApplication.class, args);
    }

    @Bean
    public static DockerClient connectDocker(){
        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://ip:2375").build();
        Info info = dockerClient.infoCmd().exec();
        String infoStr = JSONUtil.toJsonStr(info);
        System.out.println("docker的环境信息如下：=================");
        System.out.println(infoStr);
        return dockerClient;
    }
}
