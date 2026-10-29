package com.nicky.ourspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OurspaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OurspaceApplication.class, args);
	}

}
