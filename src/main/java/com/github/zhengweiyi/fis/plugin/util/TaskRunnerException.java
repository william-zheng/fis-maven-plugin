package com.github.zhengweiyi.fis.plugin.util;

public class TaskRunnerException extends Exception {
    public TaskRunnerException(String message) {
        super(message);
    }

    public TaskRunnerException(String message, Throwable cause){
        super(message, cause);
    }
}
