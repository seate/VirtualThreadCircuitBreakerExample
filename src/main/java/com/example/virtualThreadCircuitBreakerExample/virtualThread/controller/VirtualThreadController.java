package com.example.virtualThreadCircuitBreakerExample.virtualThread.controller;

import com.example.virtualThreadCircuitBreakerExample.virtualThread.service.VirtualThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VirtualThreadController {

    private final VirtualThreadService virtualThreadService;

    @GetMapping("/fail")
    public String makeFail() {
        virtualThreadService.virtualThreadService1(true);
        return "Hello World";
    }

    @GetMapping("/success")
    public String makeSuccess() {
        virtualThreadService.virtualThreadService1(false);
        return "Hello World";
    }


}
