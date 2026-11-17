package com.sp.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.SignupEmailVerificationConfirmRequest;
import com.sp.app.domain.dto.SignupEmailVerificationConfirmResponse;
import com.sp.app.domain.dto.SignupEmailVerificationRequest;
import com.sp.app.domain.dto.SignupRequest;
import com.sp.app.service.InvalidSignupEmailException;
import com.sp.app.service.InvalidSignupEmailVerificationException;
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
	@ExceptionHandler({
			InvalidSignupEmailException.class,
			InvalidSignupEmailVerificationException.class
	})
	public ResponseEntity<Void> handleInvalidSignupEmail() {
		
		return ResponseEntity.badRequest().build();
	}
	@PostMapping("/api/signup/email-verifications/confirm")
	public ResponseEntity<SignupEmailVerificationConfirmResponse> confirmVerification(
			@RequestBody SignupEmailVerificationConfirmRequest request) {
		// 서비스 호출
		String token = signupEmailVerificationService.confirmVerification(request);
		return ResponseEntity.ok(new SignupEmailVerificationConfirmResponse(token));
	}
	
	// 회원가입 회원정보
	@PostMapping("/api/signup")
	public ResponseEntity<Void> signup(
			@RequestBody SignupRequest request) {
		
		if(request.name() == null || request.name().isBlank() || request.birthDate() == null) {
			return ResponseEntity.badRequest().build();
		}
		
		signupEmailVerificationService.signup(
				request.name(),
				request.birthDate(),
				request.email(),
				request.signupVerificationToken());
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
}
