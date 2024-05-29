package com.anml.codesanbox.Judge;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RequestMapping("/code")
@RestController
public class RunCodeController {


    @Resource(name = "NativeRunCode")
    JudgeService NativeRunCode;


    @Resource(name = "DockerJavaJudgeRunCode")
    JudgeService DockerJavaJudgeRunCode;

    @Value("${judge.type}")
    public String type;

    @SneakyThrows
    @PostMapping("/run")
    public CodeJudgeResponse runCode(@RequestBody CodeJudeQuery query, HttpServletRequest request){
        String accessKey = request.getHeader("accessKey");
        String sign = request.getHeader("sign");
        String secretKey = UserValidateUtil.getSecretKey(accessKey);

        String body = JSONUtil.toJsonStr(query);
        //使用同样的算法生成签名
        String verify = SignUtils.genSign(body, secretKey);

        if(!sign.equals(verify)){
            //
            throw new IllegalArgumentException("签名错误");
        }

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
