package com.anml.codesanbox.Judge;

import com.anml.codesanbox.Judge.docker.ExecuteMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeJudgeResponse {

    private Integer code;
    private String result;

    private Long timeUsage;
    /**
     * 内存消耗
     */
    private Long memoryUsage;

    List<ExecuteMessage> executeMessageList;

    public void addExecuteMessage(ExecuteMessage executeMessage){
        if(executeMessageList==null){
            executeMessageList=new ArrayList<>();
        }
        executeMessageList.add(executeMessage);
    }

}