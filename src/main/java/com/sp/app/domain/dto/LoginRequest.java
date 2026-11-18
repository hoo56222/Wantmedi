package com.sp.app.domain.dto;

public record LoginRequest(
		String email, String password) {

	@Override
	public String toString() {
		return "LoginRequest";
	}
}
