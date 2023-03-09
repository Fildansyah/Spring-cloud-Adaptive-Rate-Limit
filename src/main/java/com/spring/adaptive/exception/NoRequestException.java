package com.spring.adaptive.exception;


public class NoRequestException extends RuntimeException {
    public NoRequestException(String message) {
        super(message);
    }
}