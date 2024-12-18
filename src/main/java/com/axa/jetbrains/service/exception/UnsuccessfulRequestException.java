package com.axa.jetbrains.service.exception;

import java.io.IOException;

public class UnsuccessfulRequestException extends IOException {
    public UnsuccessfulRequestException(String message) {
        super(message);
    }
}
