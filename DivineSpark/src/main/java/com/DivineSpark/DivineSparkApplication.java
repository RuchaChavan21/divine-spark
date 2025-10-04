package com.DivineSpark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableJpaRepositories("com.DivineSpark.repository") // clean up for cache
public class DivineSparkApplication {

	public static void main(String[] args) {
		SpringApplication.run(DivineSparkApplication.class, args);
	}

}
