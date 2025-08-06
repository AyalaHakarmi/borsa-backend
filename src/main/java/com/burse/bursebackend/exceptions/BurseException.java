package com.burse.bursebackend.exceptions;

import com.burse.bursebackend.types.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BurseException extends RuntimeException {
    private final ErrorCode errorCode;

    public BurseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
