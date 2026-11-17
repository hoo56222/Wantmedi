package com.sp.app.domain.dto;

public record SignupEmailVerificationConfirmRequest(
		String email, String code) {

}
