package com.portagecybertech.authserver;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class ApplicationIT {
    @Autowired
    private ApplicationContext context;

	@Test
	void contextLoads() {
        // Let's see in DEBUG all the beans that are loaded in the app context
	    final var beans = context.getBeanDefinitionNames();
        assertNotNull(beans);
        assertTrue(beans.length > 0);
        Stream.of(beans).forEach(log::debug);
	}
}
