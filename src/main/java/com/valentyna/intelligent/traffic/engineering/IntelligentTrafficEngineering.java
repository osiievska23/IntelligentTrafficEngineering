package com.valentyna.intelligent.traffic.engineering;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class IntelligentTrafficEngineering {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(IntelligentTrafficEngineering.class);
        builder.headless(false);
        builder.run(args);
    }
}
