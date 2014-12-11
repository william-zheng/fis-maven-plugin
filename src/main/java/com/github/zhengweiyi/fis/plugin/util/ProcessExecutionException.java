package com.github.zhengweiyi.fis.plugin.util;

public final class ProcessExecutionException extends Exception {
    public ProcessExecutionException(String message) {
        super(message);
    }
    public ProcessExecutionException(Throwable cause) {
        super(cause);
    }
}