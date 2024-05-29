package com.anml.codesanbox.Judge;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
@ConfigurationProperties(prefix = "config")
public class UserValidateUtil {
    List<AccessKeySecretKey> credentials;
    //ak和sk的map
    private static final Map<String,String> akSkMap = new HashMap<>();

    public static String getSecretKey(String accessKey) {
        return akSkMap.get(accessKey);
    }


    @PostConstruct
    public void init() {
        Map<String, String> accessKeys = this.credentials.stream().collect(Collectors.toMap(AccessKeySecretKey::getAccessKey, AccessKeySecretKey::getSecretKey));
        System.out.println("asscessKeys:");
        for (Map.Entry<String, String> entry : accessKeys.entrySet()) {
            System.out.println("Access Key: " + entry.getKey() + ", Secret Key: " + entry.getValue());
            akSkMap.put(entry.getKey(),entry.getValue());
        }
    }
//    @PostConstruct
    public void init2() {

        //初始化ak和sk的map
        String ak1 = AccessKeySecretKeyGenerator.generateAccessKey();
        String sk1 = AccessKeySecretKeyGenerator.generateSecretKey();
        akSkMap.put(ak1,sk1);
        System.out.println("Access Key: " + ak1);
        System.out.println("Secret Key: " + sk1);
        log.info("ak1:{} sk1:{}",ak1,sk1);
    }



    @Data
    public static class AccessKeySecretKey {
        private String accessKey;

        private String secretKey;
    }


}
