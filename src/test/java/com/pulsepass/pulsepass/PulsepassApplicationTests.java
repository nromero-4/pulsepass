package com.pulsepass.pulsepass;

import com.pulsepass.pulsepass.security.TestJwtSecret;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PulsepassApplicationTests {

	@DynamicPropertySource
	static void registerTestJwtSecret(DynamicPropertyRegistry registry) {
		TestJwtSecret.register(registry);
	}

	@Test
	void contextLoads() {
	}

}
