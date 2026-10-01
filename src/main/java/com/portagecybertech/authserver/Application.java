package com.portagecybertech.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.portagecybertech.authserver")
public class Application {
    public static final String APP_NAME = "portage-cybertech-auth-server";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
