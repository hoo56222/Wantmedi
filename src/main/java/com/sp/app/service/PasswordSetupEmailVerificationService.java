package com.sp.app.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.PasswordSetupEmailVerificationRow;
import com.sp.app.mail.SignupVerificationMailSender;
import com.sp.app.mapper.PasswordSetupEmailVerificationMapper;
import com.sp.app.mapper.UserMapper;

// 첫 비밀번호 설정용 인증번호 발급 서비스

@Service
public class PasswordSetupEmailVerificationService {
	
	private final UserMapper userMapper;
	private final PasswordSetupEmailVerificationMapper verificationMapper;
	private final ObjectProvider<SignupVerificationMailSender> mailSenderProvider;
	private final SecureRandom secureRandom = new SecureRandom();
	private final PasswordEncoder passwordEncoder;
	
	public PasswordSetupEmailVerificationService(
			UserMapper userMapper,
			PasswordSetupEmailVerificationMapper verificationMapper,
			ObjectProvider<SignupVerificationMailSender> mailSenderProvider,
			PasswordEncoder passwordEncoder) {
		
		this.userMapper = userMapper;
		this.verificationMapper = verificationMapper;
		this.mailSenderProvider = mailSenderProvider;
		this.passwordEncoder = passwordEncoder;
	}
	
	// 첫 비밀번호 설정용 인증번호 발급 서비스
	@Transactional
	public void issueVerification(String rawEmail) {
		// 발급 메서드에서 normalizeAndValidateEmail() 호출
		String email = EmailAddressNormalizer.normalizeAndValidate(rawEmail);
		Long userId = userMapper.findUserIdWithoutPasswordByEmail(email);
		
		if(userId == null) {
			return;
		}

		String code = String.valueOf(100_000 + secureRandom.nextInt(900_000));
		// 6자리 인증번호 문자열로 만든다.
		String secretHash = EmailVerificationCodeHasher.hash(
				"password-setup\n" + userId + "\n" + code);
		// 서버의 현재 UTC 시각을 기준으로 만료 시각 계산, 유효기간은 5분을 기준으로 시작
		OffsetDateTime expiredAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5);
		
		int insertedRows = verificationMapper.insertVerification(userId, secretHash, expiredAt); 
		
		if(insertedRows == 0) {
			return;
		}
		
		if(insertedRows != 1) {
			throw new IllegalStateException("비밀번호 설정 인증 기록을 저장하지 못했습니다.");
		}
		
		mailSenderProvider.getObject().sendPasswordSetupVerificationMail(email, code);
	}
	
	
	// 인증번호 검증 및 첫 비밀번호 저장
	private static final int MAX_FAILED_ATTEMPTS = 5;
	
	@Transactional
	public boolean confirmAndSetPassword(String rawEmail, String code, String rawPassword) {
		String email = EmailAddressNormalizer.normalizeAndValidate(rawEmail);
		
		if(code == null || !code.matches("[0-9]{6}") || rawPassword == null || rawPassword.isBlank()) {
			return false;
		}
		
		Long userId = userMapper.findUserIdWithoutPasswordByEmail(email);
		if(userId == null) {
			return false;
		}
		// 회원의 최신 인증 기록을 가져와, 인증번호를 비교해도 되는 상황인지 확인
		PasswordSetupEmailVerificationRow verification = verificationMapper.findLatestByUserId(userId);
		// 발급 기록이 없건 이미 사용한 인증번호이거나 실패횟수가 한도에 도달했을때
		if(verification == null || verification.usedAt() != null 
				|| verification.failedAttempts() >= MAX_FAILED_ATTEMPTS) {
			return false;
		}

	    HexFormat hex = HexFormat.of();
	    byte[] expectedHash = hex.parseHex(verification.secretHash());
	    byte[] submittedHash = hex.parseHex(createSecretHash(userId, code));

	    // 저장된 HMAC 해시와 입력한 인증번호로 계산한 HMAC 해시를 비교
	    if (!MessageDigest.isEqual(expectedHash, submittedHash)) {
	        verificationMapper.incrementFailedAttempts(
	                verification.verificationId(), MAX_FAILED_ATTEMPTS);
	        return false;
	    }
	    
	    if(verificationMapper.markUsed(verification.verificationId(), MAX_FAILED_ATTEMPTS) != 1) {
	    		return false;
	    }
	    
	    String passwordHash = passwordEncoder.encode(rawPassword);
	    if(userMapper.setInitialPasswordHash(userId, passwordHash) != 1) {
	    		throw new IllegalStateException("첫 비밀번호 해시를 저장하지 못했습니다.");
	    }
	    
	    		return true;
	
	}
	
	private String createSecretHash(long userId, String code) {
		return EmailVerificationCodeHasher.hash("password-setup\n" + userId + "\n" + code);
	}
}
