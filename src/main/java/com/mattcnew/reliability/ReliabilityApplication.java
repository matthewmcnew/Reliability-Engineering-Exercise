package com.mattcnew.reliability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReliabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReliabilityApplication.class, args);
	}
}
