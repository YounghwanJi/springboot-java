package com.boilerplate.springbootjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SpringbootJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootJavaApplication.class, args);
    }

}
