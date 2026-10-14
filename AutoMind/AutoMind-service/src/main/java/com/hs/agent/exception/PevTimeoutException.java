package com.hs.agent.exception;

import com.hs.common.exception.CommonException;

public class PevTimeoutException extends CommonException {

    public PevTimeoutException(String message) {
        super(message, 504);
    }
}
