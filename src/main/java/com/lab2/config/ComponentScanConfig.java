package com.lab2.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.lab2.controller",
    "com.lab2.service",
    "com.lab2.interceptor",
    "com.lab2.config"
})
public class ComponentScanConfig {
}

