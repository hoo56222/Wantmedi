package com.sp.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.SignupEmailVerificationRequest;
import com.sp.app.service.InvalidSignupEmailException;
import com.sp.app.service.SignupEmailVerificationService;

@RestController
public class SignupEmailVerificationController {
	
	private final SignupEmailVerificationService signupEmailVerificationService;

	public SignupEmailVerificationController(SignupEmailVerificationService signupEmailVerificationService) {
		this.signupEmailVerificationService = signupEmailVerificationService;
	}
	
	@PostMapping("/api/signup/email-verifications")
	public ResponseEntity<Void> issueVerification(
			@RequestBody SignupEmailVerificationRequest request) {
		signupEmailVerificationService.issueVerification(request.email());
		return ResponseEntity.noContent().build();
	}
	
	// @ExceptionHandler : 예외가 발생했을때, 어떤 메서드로 HTTP 응답을 만들지 지정
	@ExceptionHandler(InvalidSignupEmailException.class)
	public ResponseEntity<Void> handleInvalidSignupEmail() {
		
		return ResponseEntity.badRequest().build();
	}
	
}
