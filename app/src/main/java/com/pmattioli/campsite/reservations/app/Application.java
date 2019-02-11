package com.pmattioli.campsite.reservations.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.pmattioli.campsite.reservations.data.repo")
@ComponentScan("com.pmattioli.campsite.reservations")
@EntityScan("com.pmattioli.campsite.reservations.data.repo")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
