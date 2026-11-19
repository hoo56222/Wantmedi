package com.sp.app.domain.dto;

public record PasswordChangeRequest(
		String currentPassword,
		String newPassword) {

	@Override
	public String toString() {
		return "PasswordChangeRequest";
	}
}
