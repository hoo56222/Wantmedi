package com.sp.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.PasswordSetupEmailVerificationRequest;
import com.sp.app.service.InvalidSignupEmailException;
import com.sp.app.service.PasswordSetupEmailVerificationService;

@RestController
public class PasswordSetupEmailVerificationController {

	private final PasswordSetupEmailVerificationService verificationService;
	
	public PasswordSetupEmailVerificationController(
			PasswordSetupEmailVerificationService verificationService) {
		
		this.verificationService = verificationService;
	}
	
	@PostMapping("/api/password-setup/email-verifications")
	public ResponseEntity<Void> issueVerification(
			@RequestBody PasswordSetupEmailVerificationRequest request) {
		
		verificationService.issueVerification(request.email());
		
		return ResponseEntity.noContent().build();
	}
	
    @ExceptionHandler(InvalidSignupEmailException.class)
    public ResponseEntity<Void> handleInvalidEmail() {
    	
        return ResponseEntity.badRequest().build();
    }
}
