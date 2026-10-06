package com.zeynep.productapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Uygulamanin tum bean'leriyle ayaga kalkabildigini dogrular.
 * "test" profili sayesinde PostgreSQL yerine bellek ici H2 kullanilir.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
