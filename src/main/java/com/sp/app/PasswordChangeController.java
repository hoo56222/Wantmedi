package com.sp.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.PasswordChangeRequest;
import com.sp.app.service.PasswordChangeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class PasswordChangeController {

	private final PasswordChangeService passwordChangeService;
	
	public PasswordChangeController(
			PasswordChangeService passwordChangeService) {
		this.passwordChangeService = passwordChangeService;
	}
	
	// 비밀번호 변경
	@PostMapping("/api/password/change")
	public ResponseEntity<Void> changePassword(
			@RequestBody PasswordChangeRequest request,
			HttpServletRequest servletRequest) {
		
		HttpSession session = servletRequest.getSession(false);
		
		if(session == null || !(session.getAttribute("userId") instanceof Long userId)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		boolean changed = passwordChangeService.changePassword(
				userId, request.currentPassword(), request.newPassword());
		
		if(!changed) {
			return ResponseEntity.badRequest().build();
		}
		
		session.invalidate();
		
		return ResponseEntity.noContent().build();
	}
}
