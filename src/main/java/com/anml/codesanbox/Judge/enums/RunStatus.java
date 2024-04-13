package com.anml.codesanbox.Judge.enums;

public enum RunStatus {


    //提交状态  0:wati 1:running 2.ac 3.answer error,4.compileError,5.overtime,6.outOfmemory,

    Watching(0, "Watching"),
    Running(1, "Running"),
    Accepted(2, "Accepted"),
    AnswerError(3, "AnswerError"),

    CompileError(4, "CompileError"),

    Overtime(5, "Overtime"),

    OutOfMemory(6, "OutOfMemory"),
    SystemError(7, "SystemError"),
    RunErr(8, "RunErr"),
    ;


    Integer code;

    String val;


    RunStatus(int code, String val) {
        this.code = code;
        this.val = val;
    }

    //get enum by val

    public static RunStatus getEnumByVal(String val) {

        for (RunStatus runStatus : RunStatus.values()) {
            if (runStatus.val.equals(val)) {
                return runStatus;
            }
        }
        return null;
    }

    //get enum by code

    public static RunStatus getEnumByCode(int code) {

        for (RunStatus runStatus : RunStatus.values()) {
            if (runStatus.code == code) {
                return runStatus;
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
