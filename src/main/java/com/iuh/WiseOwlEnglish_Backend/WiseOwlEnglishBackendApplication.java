package com.iuh.WiseOwlEnglish_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class WiseOwlEnglishBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(WiseOwlEnglishBackendApplication.class, args);
	}

}
