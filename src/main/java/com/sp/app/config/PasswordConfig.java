package com.sp.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

// PBKDF2-HMAC-SHA256을 60만 회 반복
// 비밀번호마다 16바이트 무작위 솔트를 생성

@Configuration(proxyBeanMethods = false)
public class PasswordConfig {
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder(
				"", 16, 600_000, 
				SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
	}
}
