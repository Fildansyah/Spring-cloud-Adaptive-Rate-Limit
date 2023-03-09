package com.spring.adaptive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class ErrorPageController {
    @GetMapping("/error")
    public String errorPage(ServerWebExchange serverWebExchange) {
        return "Ups";
    }
}
