package com.sp.app.service;

public class InvalidSignupEmailVerificationException extends RuntimeException {
	public InvalidSignupEmailVerificationException() {
		super("유효한 이메일 인증 요청이 아닙니다.");
	}
}
