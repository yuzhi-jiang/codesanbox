package com.anml.codesanbox.Judge;

import com.anml.codesanbox.Judge.docker.DockerJavaJudgeRunCode;
import com.anml.codesanbox.Judge.java.BasicJavaJudgeTemplate;
import com.anml.codesanbox.Judge.java.NativeRunCode;

public class JudgeServiceStrategy {
    //根据 type 获取对应的 JudgeService
    public  static JudgeService getJudgeService(String type){
        JudgeTypeEnum byType = JudgeTypeEnum.getByType(type);
        if(byType== null){
            byType= JudgeTypeEnum.NATIVE;
        }
        return getJudgeService(byType);

    }

    public   static JudgeService getJudgeService(JudgeTypeEnum byType) {
        System.out.println("使用"+ byType.getZhType()+"方式运行代码");
        switch (byType){
            case Docker:
                return new DockerJavaJudgeRunCode();
            case NATIVE:
                return new NativeRunCode();
        }
        return null;
    }
}
