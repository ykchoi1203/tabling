package com.younggeun.tabling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TablingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TablingApplication.class, args);
    }

}
