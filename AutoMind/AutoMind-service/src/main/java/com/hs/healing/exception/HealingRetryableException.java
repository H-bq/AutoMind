package com.hs.healing.exception;

public class HealingRetryableException extends RuntimeException {

    public HealingRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
