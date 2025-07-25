package com.burse.bursebackend.exceptions;

import lombok.Getter;

@Getter
public class BurseException extends RuntimeException {
    private final ErrorCode errorCode;

    public BurseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
