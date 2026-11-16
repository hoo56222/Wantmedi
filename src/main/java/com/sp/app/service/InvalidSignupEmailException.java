package com.sp.app.service;

public class InvalidSignupEmailException extends IllegalArgumentException{

	private static final long serialVersionUID = 1L;

	public InvalidSignupEmailException(String message) {
		super(message);
	}
}
