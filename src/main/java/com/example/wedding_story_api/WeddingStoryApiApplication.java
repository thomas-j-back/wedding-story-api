package com.example.wedding_story_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeddingStoryApiApplication {

	public static void main(String[] args) {
		System.out.println("java.version=" + System.getProperty("java.version"));
		System.out.println("java.home=" + System.getProperty("java.home"));
		System.out.println("spring.version=" + org.springframework.core.SpringVersion.getVersion());
		System.out.println("SystemPropertyUtils.class=" +
				org.springframework.util.SystemPropertyUtils.class.getResource("SystemPropertyUtils.class"));
		SpringApplication.run(WeddingStoryApiApplication.class, args);
	}

}
