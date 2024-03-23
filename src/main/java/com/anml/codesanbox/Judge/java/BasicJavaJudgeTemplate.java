package com.anml.codesanbox.Judge.java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.anml.codesanbox.Judge.CodeJudeQuery;
import com.anml.codesanbox.Judge.CodeJudgeResponse;
import com.anml.codesanbox.Judge.JudgeService;

import com.anml.codesanbox.Judge.docker.ExecuteMessage;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public abstract class BasicJavaJudgeTemplate implements JudgeService {

    private static Logger logger = LoggerFactory.getLogger(BasicJavaJudgeTemplate.class);
    protected static final String CODE_TEMP_PATH=System.getProperty("user.dir")+File.separator+"codeTempPath";
    private static String reader(InputStream input) {
        StringBuilder outDat = new StringBuilder();
        try (InputStreamReader inputReader = new InputStreamReader(input, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outDat.append(line);

            }
        } catch (IOException e) {
            logger.error("command :  ,exception", e);
        }
        return outDat.toString();
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
        CodeJudgeResponse codeJudgeResponse = new CodeJudgeResponse();

        //编译代码
        String compileClass = null;
        try {
            compileClass = compileCode(query.getCode());
        } catch (IOException | InterruptedException e) {
            codeJudgeResponse.setCode(501);
            codeJudgeResponse.setError("编译错误");
            return codeJudgeResponse;
        }


        //执行代码
        runCode(compileClass, query, codeJudgeResponse);

        //delete file compileClass
        FileUtil.del(CODE_TEMP_PATH+File.separator+compileClass);

        return codeJudgeResponse;
    }

    public void runCode(String compileClass, CodeJudeQuery query, CodeJudgeResponse codeJudgeResponse) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        for (String inputStr : query.getInputList()) {
            String[] inputList = inputStr.split(" ");
            List<String> list = Arrays.asList(inputList);
            List<String> commandStr = getCommandStr(compileClass, list);
            processBuilder.command(commandStr);

            codeJudgeResponse.setOutPutStrList(new ArrayList<>());
            handerRunCode(processBuilder, codeJudgeResponse);
        }


    }

    private static void handerRunCode(ProcessBuilder processBuilder, CodeJudgeResponse codeJudgeResponse) {
        processBuilder.redirectErrorStream(false);
        try {
            //启动进程
            Process start = processBuilder.start();
            //获取输入流
            InputStream inputStream = start.getInputStream();
            String stream = reader(inputStream);
            //转成字符输入流
//            System.out.println("结果："+stream);
            InputStream errorInputStream = start.getErrorStream();
            String errStream = reader(errorInputStream);

            ExecuteMessage message = new ExecuteMessage();

            message.setMessage(stream);
            message.setErrMessage(errStream);
            codeJudgeResponse.addExecuteMessage(message);

            errorInputStream.close();
            inputStream.close();
        } catch (IOException e) {
            codeJudgeResponse.setError("执行代码出错"+e.getMessage());
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
        int exitCode = process.waitFor();
        if(exitCode!=0){
            throw new RuntimeException("编译失败");
        }
        return parentFilepath.toString();
    }
}