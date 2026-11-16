package com.sp.app.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.sp.app.mail.SignupVerificationMailSender;
import com.sp.app.mapper.SignupEmailVerificationMapper;

@Service
public class SignupEmailVerificationService {
	
	private final SignupEmailVerificationMapper signupEmailVerificationMapper;
	private final ObjectProvider<SignupVerificationMailSender> mailSenderProvider;
	
	public SignupEmailVerificationService(
	        SignupEmailVerificationMapper signupEmailVerificationMapper,
	        ObjectProvider<SignupVerificationMailSender> mailSenderProvider) {
	    this.signupEmailVerificationMapper = signupEmailVerificationMapper;
	    this.mailSenderProvider = mailSenderProvider;
	}

	// 이메일 정규화
	public String normalizeAndValidateEmail(String rawEmail) {
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
		// 정규화한 이메일을 반환한다.
		return email;
	}
	
	// 인증번호 발급 메서드
	public String issueVerification(String rawEmail) {
		// 발급 메서드에서 normalizeAndValidateEmail() 호출
		String email = normalizeAndValidateEmail(rawEmail);
		// 이메일 정규화
		SignupVerificationMailSender mailSender = mailSenderProvider.getObject();
		// 검사를 통과하면 SecureRandom으로 100000~999999 사이의 값을 뽑아
		SecureRandom random = new SecureRandom();
		String code = String.valueOf(100_000 + random.nextInt(900_000));
		// 6자리 인증번호 문자열로 만든다.
		String secretHash = createSecretHash(email, code);
		// 서버의 현재 UTC 시각을 기준으로 만료 시각 계산, 유효기간은 5분을 기준으로 시작
		OffsetDateTime expiredAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5);
		// insertVerification()에 정규화된 이메일, secretHash, 만료 시각을 넘긴다.
		signupEmailVerificationMapper.insertVerification(email, secretHash, expiredAt);
		// 이메일 발송
		mailSender.sendVerificationMail(email, code);
		// 저장에 성공한 뒤 인증번호를 반환한다.
		return code;
	}
	
	// HMAC-SHA256 : 비밀키와 메시지를 결합하여 해시 값을 생성
	// -- 데이터 무결성 보장, 복호화 불가능, 안전함
	private String createSecretHash(String email, String code) {
		// 환경변수에는 무작위로 생성한 32바이트 키를 Base64로 인코딩해 넣는다.
		String encodedKey = System.getenv("SIGNUP_VERIFICATION_HMAC_KEY");
		
		if(encodedKey == null || encodedKey.isBlank()) {
			throw new IllegalArgumentException("이메일 인증 HMAC 키가 설정되지 않았습니다.");
		}
		
		try {
			// 이메일 인증 HMAC 키는 32byte 이상이여야 함
			byte[] key = Base64.getDecoder().decode(encodedKey);
			if(key.length < 32) {
				throw new IllegalArgumentException("이메일 인증 HMAC 키는 32byte 이상이어야 합니다.");
			}
			
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key, "HmacSHA256"));
			
			byte[] hash = mac.doFinal((email + "\n" + code).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (IllegalArgumentException | GeneralSecurityException e) {
			throw new IllegalArgumentException("이메일 인증 HMAC 키 설정을 확인해주세요.", e);
		}
	}
	
}
