package com.sp.app.domain.dto;

public record PasswordSetupEmailVerificationConfirmRequest(
		String email, String code, String password) {

	@Override
	public String toString() {
		return "PasswordSetupEmailVerificationConfirmRequest";
	}
}
