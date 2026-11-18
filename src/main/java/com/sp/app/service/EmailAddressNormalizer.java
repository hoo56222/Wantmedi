package com.sp.app.service;

import java.util.Locale;

// 이메일 정규화 & 형식 검사 공통 클래스

public final class EmailAddressNormalizer {
	
	private EmailAddressNormalizer() {
		
	}
	
	public static String normalizeAndValidate(String rawEmail) {
		// rawEmail : 전달받은 이메일
		// 전달받은 이메일이 null 이거나 공백뿐이면 IllegalArgumentException
		if(rawEmail == null || rawEmail.isBlank()) {
			throw new InvalidSignupEmailException("이메일을 입력해주세요.");
		}
		// strip()으로 이메일 앞뒤 공백을 제거, 소문자로 변환 
		String email = rawEmail.strip().toLowerCase(Locale.ROOT);
		// 정규화한 이메일이 254자를 넘거나 이메일 형식에 맞지 않으면 IllegalArgumentException
		if(email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
			throw new InvalidSignupEmailException("이메일 형식이 올바르지 않습니다.");
		}
		
		return email;
	}
}
