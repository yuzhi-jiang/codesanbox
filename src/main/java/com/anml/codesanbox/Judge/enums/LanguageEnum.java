package com.anml.codesanbox.Judge.enums;

public enum LanguageEnum {

    JAVA(1, "java"),
    PYTHON(2, "python"),
    CPP(3, "cpp"),
    ;

    Integer code;

    String val;


    LanguageEnum(Integer code, String val) {
        this.code = code;
        this.val = val;
    }

    public static LanguageEnum getEnumByVal(String val) {

        for (LanguageEnum LanguageEnum : LanguageEnum.values()) {
            if (LanguageEnum.val.equals(val)) {
                return LanguageEnum;
            }
        }
        return null;
    }

    //get enum by code

    public static LanguageEnum getEnumByCode(int code) {

        for (LanguageEnum LanguageEnum : LanguageEnum.values()) {
            if (LanguageEnum.code == code) {
                return LanguageEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getVal() {
        return val;
    }
}
