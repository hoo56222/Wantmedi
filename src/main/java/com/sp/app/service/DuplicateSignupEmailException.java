package com.sp.app.service;

public class DuplicateSignupEmailException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	// 중복 가입 예외
	public DuplicateSignupEmailException() {
		super("이미 가입한 이메일입니다.");
	}
}
