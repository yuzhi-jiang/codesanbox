package com.anml.codesanbox.Judge;

import com.anml.codesanbox.Judge.docker.ExecuteMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeJudgeResponse {

    private Integer code;
    private String result;

    private String error;
    private String success;
    private String message;
    private String stackTrace;
    private String time;
    private String memory;
    private String language;
    private String version;
    private String compilerVersion;
    private String compilerOptions;
    private String compilerFlags;
    private String compilerOutput;
    private String compilerError;
    private String compilerWarning;
    private String compilerInfo;
    private String compilerDebug;
    private String compilerStackTrace;

    List<String> outPutStrList;
    List<ExecuteMessage> executeMessageList;

    public void addExecuteMessage(ExecuteMessage executeMessage){
        if(executeMessageList==null){
            executeMessageList=new ArrayList<>();
        }
        executeMessageList.add(executeMessage);
    }

}
