package com.fortagym;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.fortagym.config.TestBeansConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestBeansConfig.class)
class FortaGymApplicationTests {

	@Test
	void contextLoads() {
	}

}
