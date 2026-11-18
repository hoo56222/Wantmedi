package com.sp.app.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// HMAC 계산 공통 클래스
// 가입 & 첫 비밀번호 설정
// HMAC-SHA256 : 비밀키와 메시지를 결합하여 해시 값을 생성
// -- 데이터 무결성 보장, 복호화 불가능, 안전함

public class EmailVerificationCodeHasher {

	private static final String ALGORITHM = "HmacSHA256";
	
	private EmailVerificationCodeHasher() {
		
	}
	
	public static String hash(String message) {
		String encodedKey = System.getenv("SIGNUP_VERIFICATION_HMAC_KEY");
		if(encodedKey == null || encodedKey.isBlank()) {
			throw new IllegalArgumentException("이메일 인증 HMAC 키가 설정되지 않았습니다.");
		}
		
		byte[] key;
		
		try {
			key = Base64.getDecoder().decode(encodedKey);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("이메일 인증 HMAC 키의 Base64 형식을 확인해주세요.");
		}
		
		if(key.length < 32) {
			throw new IllegalArgumentException("이메일 인증 HMAC 키는 32바이트 이상이어야 합니다.");
		}
		
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(key, ALGORITHM));
			
			return HexFormat.of().formatHex(
					mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException  e) {
			throw new IllegalArgumentException("이메일 인증 HMAC 계산에 실패했습니다.", e);
		}
	}
}
