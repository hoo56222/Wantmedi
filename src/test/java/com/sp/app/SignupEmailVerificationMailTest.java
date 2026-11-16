package com.sp.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.sp.app.service.SignupEmailVerificationService;

// @Autowired : 필드를 이용한 생성자 주입방식

@SpringBootTest
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "WANTMEDI_RUN_SMTP_TEST", matches = "true")
public class SignupEmailVerificationMailTest {
	
	private final SignupEmailVerificationService signupEmailVerificationService;
	private final String receiverEmail;
	
	@Autowired
	public SignupEmailVerificationMailTest(
			SignupEmailVerificationService signupEmailVerificationService,
			@Value("${spring.mail.username}") String receiverEmail) {
		this.signupEmailVerificationService = signupEmailVerificationService;
		this.receiverEmail = receiverEmail;
	}
	
	@Test
	public void sendsVerificationMail() {
		signupEmailVerificationService.issueVerification(receiverEmail);
	}
}
