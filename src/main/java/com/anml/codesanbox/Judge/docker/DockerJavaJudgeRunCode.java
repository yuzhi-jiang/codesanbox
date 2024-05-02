package com.anml.codesanbox.Judge.docker;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.anml.codesanbox.Judge.CodeJudeQuery;
import com.anml.codesanbox.Judge.CodeJudgeResponse;
import com.anml.codesanbox.Judge.enums.RunStatus;
import com.anml.codesanbox.Judge.java.BasicJavaJudgeTemplate;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
@Component("DockerJavaJudgeRunCode")
public class DockerJavaJudgeRunCode extends BasicJavaJudgeTemplate {

    @Value("${docker.serverUrl}")
    private String serverUrl;

    private static volatile DockerClient staticDockerClient;

    static {
//        staticDockerClient =connectDocker();
    }

//    @Autowired
    public  void setStaticDockerClient(DockerClient dockerClient) {
        DockerJavaJudgeRunCode.staticDockerClient = dockerClient;
    }

    public  DockerClient connectDocker(){
        DockerClient dockerClient = null;
        try {
            dockerClient = DockerClientBuilder.getInstance(serverUrl).build();
            Info info = dockerClient.infoCmd().exec();
            String infoStr = JSONUtil.toJsonStr(info);
            System.out.println("docker的环境信息如下：=================");
            System.out.println(infoStr);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接docker异常");
            return null;
        }
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


    private void checkDockerClientConnect(){
        if(staticDockerClient==null){
            synchronized (DockerJavaJudgeRunCode.class){
                if(staticDockerClient==null){
                    staticDockerClient = connectDocker();
                }
            }
        }
        if(staticDockerClient==null){
            throw new RuntimeException("docker连接失败");
        }
    }

    @Override
    public void runCode(String parentFilepath, CodeJudeQuery codeJudeQuery, CodeJudgeResponse codeJudgeResponse) {
        checkDockerClientConnect();
        if(NumberUtil.compare(codeJudeQuery.getMemoryLimit(),(6*1000*1000))<0){
            codeJudgeResponse.setCode(RunStatus.OutOfMemory.getCode());
            codeJudgeResponse.setResult("Minimum memory limit allowed is 6MB");
            throw new BadRequestException("Status 400: Minimum memory limit allowed is 6MB");
        }


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
        CreateContainerCmd javaRunContainer = cmd.withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true);

        CreateContainerResponse exec = javaRunContainer.exec();

        //容器id
        String containerId = exec.getId();


        dockerClient.startContainerCmd(containerId).exec();



        List<ExecuteMessage> executeMessageList = new CopyOnWriteArrayList<>();


        for (String inputStr : codeJudeQuery.getInputList()) {

            final Long[] maxMemory = {0L};

            final boolean[] unFinish = {true};
//            String[] inputList = new String[]{"1","2"};
            String[] inputList = inputStr.split(" ");
            List<String> commandStr = getCommandStr(parentFilepath, Arrays.asList(inputList));

            System.out.println("执行命令："+commandStr);
            ExecCreateCmdResponse exec1 = dockerClient.execCreateCmd(containerId)
//                    .withTty(true)
                    .withCmd(commandStr.toArray(new String[0]))
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            System.out.println("创建了执行命令");
            ExecuteMessage message=new ExecuteMessage();
            message.setMessage("");
            message.setErrMessage("");



            StatsCmd statsCmd = dockerClient.statsCmd(containerId);

            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用情况: " + statistics.getMemoryStats().getUsage());
//                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                    maxMemory[0]=statistics.getMemoryStats().getUsage();
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            }).onComplete();
            statsCmd.close();



            StopWatch stopWatch = new StopWatch(inputStr);

            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback(){
                @Override
                public void onNext(Frame frame) {
                    String resStr = new String(frame.getPayload(),StandardCharsets.UTF_8);
//                    System.out.println("执行结果："+resStr+":a");
                    if (frame.getStreamType().equals(StreamType.STDERR)) {
                        //错误信息

                        message.setErrMessage(message.getErrMessage()+resStr);

                    }
                    else {
                        //正常信息
                        message.setMessage(message.getMessage()+resStr);
                    }
                    unFinish[0] =false;
                    super.onNext(frame);
                }

                @Override
                public void onComplete() {
                    unFinish[0]=false;
                    stopWatch.stop();
                    super.onComplete();
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("callbackonError:"+throwable.getMessage());
                }

                @Override
                public void onStart(Closeable stream) {
                    stopWatch.start();
                    super.onStart(stream);
                }
            };
            try {

                boolean flag = dockerClient.execStartCmd(exec1.getId()).exec(execStartResultCallback).awaitCompletion(codeJudeQuery.getTimeLimit(), TimeUnit.MILLISECONDS);

                if(!flag&&unFinish[0]){
                    //错误信息
                    message.setErrMessage("执行失败：超时");
                    stopWatch.stop();
                }
                message.setExecuteTime(stopWatch.getTotalTimeMillis());
                message.setMemoryUsage(maxMemory[0]/1024);

                String resStr = message.getMessage();
                System.out.println("输出结果是："+resStr);
                if(resStr.endsWith("\n")){
                    resStr=resStr.substring(0,resStr.lastIndexOf("\n"));
                }
                if(resStr.endsWith("\r\n")){
                    resStr=resStr.substring(0,resStr.lastIndexOf("\r\n"));
                }
                message.setMessage(resStr);
                System.out.println("换行后结果是："+resStr);
                resStr = message.getErrMessage();
                if(resStr.endsWith("\n")){
                    resStr=resStr.substring(0,resStr.lastIndexOf("\n"));
                }
                if(resStr.endsWith("\r\n")){
                    resStr=resStr.substring(0,resStr.lastIndexOf("\r\n"));
                }
                message.setErrMessage(resStr);
                executeMessageList.add(message);
            } catch (InterruptedException e) {

                //错误信息
                message.setErrMessage("执行失败");
                executeMessageList.add(message);
            }
        }
        codeJudgeResponse.setExecuteMessageList(executeMessageList);
//        dockerClient.removeContainerCmd(containerId).withForce(true).exec();

    }
}
