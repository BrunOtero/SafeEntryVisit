package com.safeentry.Visits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka; // Adicione este import

@SpringBootApplication
@EnableKafka // Adicione esta anotação
public class SafeEntryVisitsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafeEntryVisitsApplication.class, args);
	}

}