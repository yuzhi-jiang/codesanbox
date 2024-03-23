package com.anml.codesanbox.Judge.docker;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.anml.codesanbox.Judge.CodeJudeQuery;
import com.anml.codesanbox.Judge.CodeJudgeResponse;
import com.anml.codesanbox.Judge.java.BasicJavaJudgeTemplate;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.rmi.CORBA.Util;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DockerJavaJudgeRunCode extends BasicJavaJudgeTemplate {


//    @Resource
    private static DockerClient staticDockerClient;

    static {
        staticDockerClient =connectDocker();
    }



    public static DockerClient connectDocker(){
        DockerClient dockerClient = DockerClientBuilder.getInstance("tcp://127.0.0.1:2375").build();
        Info info = dockerClient.infoCmd().exec();
        String infoStr = JSONUtil.toJsonStr(info);
        System.out.println("docker的环境信息如下：=================");
        System.out.println(infoStr);
        return dockerClient;
    }
   static boolean FIRST_INIT=false;
    public static void main(String[] args) throws IOException, InterruptedException {
        String code = ResourceUtil.readStr("Main.java", StandardCharsets.UTF_8);
        CodeJudeQuery codeJudeQuery = new CodeJudeQuery();
        codeJudeQuery.setCode(code);
        codeJudeQuery.setMemoryLimit(100 * 1000 * 1000L);
        codeJudeQuery.setInputList(Arrays.asList("1 1", "2 3","234 21"));
        String parentFilepath = compileCode(codeJudeQuery.getCode());

        DockerClient dockerClient = staticDockerClient;

        String image = "openjdk:8-alpine";
        if (FIRST_INIT){
            PullImageCmd pullImageCmd = staticDockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像 " + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
            System.out.println("下载完成");
            FIRST_INIT = false;
        }


        CreateContainerCmd cmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(codeJudeQuery.getMemoryLimit());
        hostConfig.withCpuCount(1L);
        hostConfig.withReadonlyRootfs(true);
        hostConfig.setBinds(new Bind(CODE_TEMP_PATH,new Volume("/app")));
        CreateContainerCmd javaRunContainer = cmd.withHostConfig(hostConfig).withNetworkDisabled(true).withAttachStdin(true).withAttachStdout(true)
                .withAttachStderr(true).withTty(true);

        CreateContainerResponse exec = javaRunContainer.exec();

        //容器id
        String containerId = exec.getId();


        List<ExecuteMessage> messageList=new ArrayList<>();


        dockerClient.startContainerCmd(containerId).exec();

        for (String inputStr : codeJudeQuery.getInputList()) {
//            String[] inputList = new String[]{"1","2"};
            String[] inputList = inputStr.split(" ");
            List<String> commandStr = getCommandStr(parentFilepath, Arrays.asList(inputList));

            System.out.println("执行命令："+commandStr);
            ExecCreateCmdResponse exec1 = dockerClient.execCreateCmd(containerId).withTty(true).withCmd(commandStr.toArray(new String[0])).withTty(true).withAttachStdin(true).withAttachStdout(true)
                    .withAttachStderr(true).exec();

            System.out.println("创建了执行命令");
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback(){
                @Override
                public void onNext(Frame frame) {
//                    System.out.println("执行结果"+new String(frame.getPayload()));
                    if (frame.getStreamType().equals(StreamType.STDERR)) {
                        ExecuteMessage message=new ExecuteMessage();
                        //错误信息
                        message.setErrMessage(new String(frame.getPayload()));

                        messageList.add(message);
                    }
                    else {
                        ExecuteMessage message=new ExecuteMessage();
                        //正常信息
                        message.setMessage(new String(frame.getPayload()));
                        messageList.add(message);
                    }
                    super.onNext(frame);
                }
            };
            dockerClient.execStartCmd(exec1.getId()).exec(execStartResultCallback).awaitCompletion();
        }

        dockerClient.removeContainerCmd(containerId).withForce(true).exec();

        System.out.println("messageList");
        messageList.forEach(System.out::println);

        FileUtil.del(CODE_TEMP_PATH+ File.separator+parentFilepath);

    }
      public static List<String> getCommandStr(String parentFilepath, List<String> inputList) {

        List<String> commandStr=new ArrayList<>();
        commandStr.add("java");
        commandStr.add("-Dfile.encoding=utf-8");
        commandStr.add("-cp");
        commandStr.add("/app/"+parentFilepath);
        commandStr.add("Main");

        commandStr.addAll(inputList);
        return commandStr;

    }

    @Override
    public void runCode(String parentFilepath, CodeJudeQuery codeJudeQuery, CodeJudgeResponse codeJudgeResponse) {

        DockerClient dockerClient = staticDockerClient;

        String image = "openjdk:8-alpine";
        if (FIRST_INIT){
            PullImageCmd pullImageCmd = staticDockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像 " + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
            System.out.println("下载完成");
            FIRST_INIT = false;
        }


        CreateContainerCmd cmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(codeJudeQuery.getMemoryLimit());
        hostConfig.withCpuCount(1L);
        hostConfig.withReadonlyRootfs(true);
        hostConfig.setBinds(new Bind(CODE_TEMP_PATH,new Volume("/app")));
        CreateContainerCmd javaRunContainer = cmd.withHostConfig(hostConfig).withNetworkDisabled(true).withAttachStdin(true).withAttachStdout(true)
                .withAttachStderr(true).withTty(true);

        CreateContainerResponse exec = javaRunContainer.exec();

        //容器id
        String containerId = exec.getId();


        dockerClient.startContainerCmd(containerId).exec();

        for (String inputStr : codeJudeQuery.getInputList()) {
//            String[] inputList = new String[]{"1","2"};
            String[] inputList = inputStr.split(" ");
            List<String> commandStr = getCommandStr(parentFilepath, Arrays.asList(inputList));

            System.out.println("执行命令："+commandStr);
            ExecCreateCmdResponse exec1 = dockerClient.execCreateCmd(containerId).withTty(true).withCmd(commandStr.toArray(new String[0])).withTty(true).withAttachStdin(true).withAttachStdout(true)
                    .withAttachStderr(true).exec();

            System.out.println("创建了执行命令");
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback(){
                @Override
                public void onNext(Frame frame) {
//                    System.out.println("执行结果："+new String(frame.getPayload()));
                    if (frame.getStreamType().equals(StreamType.STDERR)) {
                        //错误信息
                        ExecuteMessage message=new ExecuteMessage();
                        message.setErrMessage(new String(frame.getPayload()));
                        codeJudgeResponse.addExecuteMessage(message);
                    }
                    else {
                        //正常信息
                        ExecuteMessage message=new ExecuteMessage();
                        message.setMessage(new String(frame.getPayload()));
                        codeJudgeResponse.addExecuteMessage(message);
                    }
                    super.onNext(frame);
                }
            };
            try {
                boolean flag = dockerClient.execStartCmd(exec1.getId()).exec(execStartResultCallback).awaitCompletion(codeJudeQuery.getTimeLimit(), TimeUnit.MILLISECONDS);
                if(!flag){
                    ExecuteMessage message=new ExecuteMessage();
                    //错误信息
                    message.setErrMessage("执行失败：超时");
                    codeJudgeResponse.addExecuteMessage(message);
                }
            } catch (InterruptedException e) {
                ExecuteMessage message=new ExecuteMessage();
                //错误信息
                message.setErrMessage("执行失败");
                codeJudgeResponse.addExecuteMessage(message);
            }
        }
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();

    }
}
