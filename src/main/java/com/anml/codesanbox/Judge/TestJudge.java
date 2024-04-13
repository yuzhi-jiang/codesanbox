package com.anml.codesanbox.Judge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.anml.codesanbox.Judge.docker.DockerJavaJudgeRunCode;
import com.anml.codesanbox.Judge.docker.ExecuteMessage;
import com.anml.codesanbox.Judge.java.NativeRunCode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TestJudge {
    public static void main(String[] args) {
        String code = ResourceUtil.readStr("Main.java", StandardCharsets.UTF_8);


        JudgeTypeEnum type=JudgeTypeEnum.NATIVE;
        JudgeService judgeService=JudgeServiceStrategy.getJudgeService(type);
        if(judgeService==null){
            throw new NullPointerException("无法获取代码沙箱");
        }
        CodeJudeQuery codeJudeQuery = new CodeJudeQuery();
        codeJudeQuery.setMemoryLimit(1024  * 1024 * 6);
        codeJudeQuery.setTimeLimit(50*2*1000);
        codeJudeQuery.setCode(code);
        codeJudeQuery.setInputList(Arrays.asList("1 1","2 3"));
        CodeJudgeResponse judge = judgeService.judge(codeJudeQuery);
        System.out.println("judgeInfo：");
        System.out.println(judge);
        System.out.println("判断题结果明细：");

        if(CollUtil.isEmpty(judge.getExecuteMessageList())){
            return;
        }
        for (ExecuteMessage message : judge.getExecuteMessageList()) {
            System.out.println(message);
        }



    }
}
