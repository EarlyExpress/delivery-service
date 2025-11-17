package com.early_express.delivery_service.global.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/test")
    public String test() {
        return "Default Server is working!";
    }
}
