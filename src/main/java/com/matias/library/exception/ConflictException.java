package com.matias.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}