package com.anml.codesanbox.Judge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/code")
@RestController
public class RunCodeController {


    @Resource(name = "NativeRunCode")
    JudgeService NativeRunCode;


    @Resource(name = "DockerJavaJudgeRunCode")
    JudgeService DockerJavaJudgeRunCode;

    @Value("${judge.type}")
    public String type;

    @PostMapping("/run")
    public CodeJudgeResponse runCode(@RequestBody CodeJudeQuery query){
//        System.out.println("hello world");

        if(type.equals("navite")){
            return NativeRunCode.judge(query);
        } else if (type.equals("docker")) {
            return DockerJavaJudgeRunCode.judge(query);
        }
        throw new NullPointerException("无法获取judge.type的值");
    }

    @PostMapping("/run/{typeT}")
    public CodeJudgeResponse runCode1(@PathVariable String typeT,@RequestBody CodeJudeQuery query){
//        System.out.println("hello world");

        if(typeT.equals("navite")){
            return NativeRunCode.judge(query);
        } else if (typeT.equals("docker")) {
            return DockerJavaJudgeRunCode.judge(query);
        }
        throw new NullPointerException("无法获取judge.type的值");
    }

}
