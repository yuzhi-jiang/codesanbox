package com.anml.codesanbox.Judge.java;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.anml.codesanbox.Judge.CodeJudeQuery;
import com.anml.codesanbox.Judge.CodeJudgeResponse;
import com.anml.codesanbox.Judge.JudgeService;

import com.anml.codesanbox.Judge.docker.ExecuteMessage;
import com.anml.codesanbox.Judge.enums.RunStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 模板方法
 * 实现 judgeService 接口
 */
@Slf4j
public abstract class BasicJavaJudgeTemplate implements JudgeService {

    private static Logger logger = LoggerFactory.getLogger(BasicJavaJudgeTemplate.class);
    protected static final String CODE_TEMP_PATH=System.getProperty("user.dir")+File.separator+"codeTempPath";
    private static String reader(InputStream input) {
        List<String> outDat=new ArrayList<>();
        try (InputStreamReader inputReader = new InputStreamReader(input, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outDat.add(line);

            }
        } catch (IOException e) {
            logger.error("command :  ,exception", e);
        }
        return String.join("\n",outDat);
    }

    public static void main(String[] args) throws IOException, InterruptedException {




    }



    @SneakyThrows
    @Override
    public CodeJudgeResponse judge(CodeJudeQuery query) {

        // 1. 解析 query
        // 2. 生成临时文件
        // 3. 执行编译
        // 4. 执行代码
        // 5. 返回结果
        CodeJudgeResponse codeJudgeResponse = null;
        String compileClass = null;
        try {
            codeJudgeResponse = new CodeJudgeResponse();

            //编译代码
            compileClass = null;
            try {
                compileClass = compileCode(query.getCode());
            } catch (Exception e) {
                codeJudgeResponse.setCode(RunStatus.CompileError.getCode());
                codeJudgeResponse.setResult("编译错误"+e.getMessage());
                return codeJudgeResponse;
            }


            //执行代码
            runCode(compileClass, query, codeJudgeResponse);
            codeJudgeResponse.setCode(RunStatus.Accepted.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            if(codeJudgeResponse.getCode()==null){
                codeJudgeResponse.setCode(RunStatus.SystemError.getCode());
            }
            if(codeJudgeResponse.getResult()==null){
                codeJudgeResponse.setResult("编译错误"+e.getMessage());
            }
            return codeJudgeResponse;
        }

        finally {
            //delete file compileClass
            FileUtil.del(CODE_TEMP_PATH+File.separator+compileClass);
        }


//        System.out.println("judeinfo");
//        System.out.println(codeJudgeResponse);
        log.info("res judeinfo:{}",codeJudgeResponse);
        return codeJudgeResponse;
    }

    public void runCode(String compileClass, CodeJudeQuery query, CodeJudgeResponse codeJudgeResponse) {
        codeJudgeResponse.setExecuteMessageList(new ArrayList<>());
        ProcessBuilder processBuilder = new ProcessBuilder();
        for (String inputStr : query.getInputList()) {
            String[] inputList = inputStr.split(" ");
            List<String> list = Arrays.asList(inputList);
            List<String> commandStr = getCommandStr(compileClass, list);
            processBuilder.command(commandStr);


            handerRunCode(processBuilder, codeJudgeResponse);
        }


    }

    private static void handerRunCode(ProcessBuilder processBuilder, CodeJudgeResponse codeJudgeResponse) {
        processBuilder.redirectErrorStream(false);
        try {

            StopWatch stopWatch = new StopWatch();
            //启动进程
            stopWatch.start();
            Process start = processBuilder.start();
            //获取输入流
            InputStream inputStream = start.getInputStream();
            String stream = reader(inputStream);
            //转成字符输入流
//            System.out.println("结果："+stream);
            InputStream errorInputStream = start.getErrorStream();
            String errStream = reader(errorInputStream);
            stopWatch.stop();
            ExecuteMessage message = new ExecuteMessage();

            message.setMessage(stream);
            message.setErrMessage(errStream);
            message.setExecuteTime(stopWatch.getTotalTimeMillis());
            codeJudgeResponse.addExecuteMessage(message);

            errorInputStream.close();
            inputStream.close();
        } catch (IOException e) {
            codeJudgeResponse.setResult("执行代码出错"+e.getMessage());
            codeJudgeResponse.setCode(RunStatus.RunErr.getCode());
        }
    }

    protected static List<String> getCommandStr(String compileClassParentPath, List<String> inputList) {

        List<String> commandStr=new ArrayList<>();
        commandStr.add("java");
        commandStr.add("-Dfile.encoding=utf-8");
        commandStr.add("-cp");
        commandStr.add(CODE_TEMP_PATH+File.separator+compileClassParentPath);
        commandStr.add("Main");

        commandStr.addAll(inputList);
        return commandStr;

    }

    protected static String compileCode(String code) throws IOException, InterruptedException {
        //将代码写入文件 到 codeTemplate目录
        UUID parentFilepath = UUID.randomUUID();
        String parentPath = CODE_TEMP_PATH+File.separator+ parentFilepath;

        String fileName= "Main.java";
        if(!FileUtil.exist(parentPath)){
            FileUtil.mkdir(parentPath);
        }
        File file = FileUtil.file(parentPath, fileName);
        try(FileOutputStream fos=new FileOutputStream(file)){
            fos.write(code.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String compileExec = String.format("javac -encoding utf-8 %s%sMain.java", parentPath,File.separator);

        Process process = Runtime.getRuntime().exec( compileExec );
        InputStream errorStream = process.getErrorStream();
        String reader = reader(errorStream);
        int exitCode = process.waitFor();
        if(exitCode!=0){
//            System.out.println("reader:");
//            System.out.println(reader);
            FileUtil.del(CODE_TEMP_PATH+File.separator+parentFilepath);
            throw new RuntimeException("编译失败:"+reader);
        }
        return parentFilepath.toString();
    }
}
