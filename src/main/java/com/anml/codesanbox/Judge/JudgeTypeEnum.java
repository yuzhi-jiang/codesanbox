package com.anml.codesanbox.Judge;

public enum JudgeTypeEnum {

    NATIVE("native","原生"),
    Docker("docker","docker容器");

    String type;
    String ZhType;

    JudgeTypeEnum(String type,String ZhType){

        this.type = type;
        this.ZhType = ZhType;
    }

    public String getType() {
        return type;
    }

    public String getZhType() {
        return ZhType;
    }

    //根据 type 获取枚举

    public static JudgeTypeEnum getByType(String type){

        for(JudgeTypeEnum judgeTypeEnum : JudgeTypeEnum.values()){
            if(judgeTypeEnum.type.equals(type)){
                return judgeTypeEnum;
            }
        }

        return null;
    }
}
