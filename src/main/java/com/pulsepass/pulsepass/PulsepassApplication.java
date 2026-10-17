package com.pulsepass.pulsepass;

import com.pulsepass.pulsepass.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class PulsepassApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulsepassApplication.class, args);
	}

}
