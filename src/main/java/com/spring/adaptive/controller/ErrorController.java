package com.spring.adaptive.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorController {
    @ExceptionHandler(RequestNotPermitted.class)
    public String requestNotPermitted(RequestNotPermitted requestNotPermitted) {
        return "Ups";
    }
}
