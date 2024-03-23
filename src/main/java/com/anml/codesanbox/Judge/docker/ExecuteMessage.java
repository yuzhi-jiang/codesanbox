package com.anml.codesanbox.Judge.docker;

import lombok.Data;

@Data
public class ExecuteMessage {
    String message;
    String errMessage;

    public ExecuteMessage() {
    }

    public ExecuteMessage(String message, String errMessage) {
        this.message = message;
        this.errMessage = errMessage;
    }

    public static ExecuteMessage sucessExecuteMessage(String message) {
        return new ExecuteMessage(message, null);
    }
    public static ExecuteMessage errExecuteMessage(String message) {
        return new ExecuteMessage(null, message);
    }
}
