package org.example.elearningbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class ELearningBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ELearningBeApplication.class, args);
    }

}
