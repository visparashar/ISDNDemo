package com.vp.work.isbn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages={"com.vp.work.isbn"})
public class DemoApplication {	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
