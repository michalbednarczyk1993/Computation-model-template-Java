package com.setcom.computation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class ComputationTemplateApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ComputationTemplateApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// pzygotowanie podstawoweg stanu bazy, to co sie dzieje po starcie
	}
}


