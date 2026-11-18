package com.sp.app.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	public PasswordSetupEmailVerificationService(
			UserMapper userMapper,
			PasswordSetupEmailVerificationMapper verificationMapper,
			ObjectProvider<SignupVerificationMailSender> mailSenderProvider) {
		
		this.userMapper = userMapper;
		this.verificationMapper = verificationMapper;
		this.mailSenderProvider = mailSenderProvider;
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
}
