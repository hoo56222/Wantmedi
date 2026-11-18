package com.sp.app.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.NewUser;
import com.sp.app.domain.dto.SignupEmailVerificationConfirmRequest;
import com.sp.app.domain.dto.SignupEmailVerificationRow;
import com.sp.app.mail.SignupVerificationMailSender;
import com.sp.app.mapper.SignupEmailVerificationMapper;
import com.sp.app.mapper.SignupVerificationTokenMapper;
import com.sp.app.mapper.UserMapper;

@Service
public class SignupEmailVerificationService {
	
	private final SignupEmailVerificationMapper signupEmailVerificationMapper;
	private final ObjectProvider<SignupVerificationMailSender> mailSenderProvider;
	private final SignupVerificationTokenMapper signupVerificationTokenMapper;
	private final UserMapper userMapper;
	
	public SignupEmailVerificationService(
	        SignupEmailVerificationMapper signupEmailVerificationMapper,
	        ObjectProvider<SignupVerificationMailSender> mailSenderProvider,
	        SignupVerificationTokenMapper signupVerificationTokenMapper,
	        UserMapper userMapper) {
	    this.signupEmailVerificationMapper = signupEmailVerificationMapper;
	    this.mailSenderProvider = mailSenderProvider;
	    this.signupVerificationTokenMapper = signupVerificationTokenMapper;
	    this.userMapper = userMapper;
	}

	// 이메일 정규화
	public String normalizeAndValidateEmail(String rawEmail) {

		return EmailAddressNormalizer.normalizeAndValidate(rawEmail);
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
	
	// 최신의 인증번호 확인 메서드
	@Transactional
	public String confirmVerification(SignupEmailVerificationConfirmRequest request) {
		String email = normalizeAndValidateEmail(request.email());
		SignupEmailVerificationRow verification = signupEmailVerificationMapper.findLatestByEmail(email);
	
		// 조회 결과가 null 이거나 usedAt() 에 값이 있으면 
		if(verification == null || verification.usedAt() != null) {
			// 인증 실패 처리
			throw new InvalidSignupEmailVerificationException();
		}
		
		// expiresAt() 이 현재 시각과 같거나 이전이면
		if(!verification.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
			throw new InvalidSignupEmailVerificationException();
		}
		
		// 인증번호 비교 검증
		// 형식이 올바른 6자리여도, 발급한 번호와 다르면 예외발생
		HexFormat hex = HexFormat.of();
		// expectedHash : 서버가 인증번호를 발급할 때 DB에서 읽어온 HMAC
		// submittedHash : 사용자가 제출한 인증번호로 서버가 계산한 HMAC
		byte[] expectedHash = hex.parseHex(verification.secretHash());
		// 요청 인증번호(code) 를 넘겨야함
		byte[] submittedHash = hex.parseHex(createSecretHash(email, request.code()));
		
		if(!java.security.MessageDigest.isEqual(expectedHash, submittedHash)) {
			throw new InvalidSignupEmailVerificationException();
		}
		
		// markUsed 연결
		int updateRows = signupEmailVerificationMapper.markUsed(verification.verificationId());
		// 갱신 건수가 0이면 예외발생
		if(updateRows != 1) {
			throw new InvalidSignupEmailVerificationException();
		}
		
		// 해시, 토큰 만료 유효시간
		String token = generateSignupVerificationToken();
		String tokenHash = hashSignupVerificationToken(token);
		OffsetDateTime tokenExpiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10);
		
		int insertRows = signupVerificationTokenMapper.insertToken(tokenHash, email, tokenExpiresAt);
		
		if(insertRows != 1) {
			throw new IllegalStateException();
		}
		
		return token;
	}
	
	// 가입용 토큰 원문을 생성하는 메서드
	private String generateSignupVerificationToken() {
		byte[] tokenBytes = new byte[32];
		SecureRandom random = new SecureRandom();
		random.nextBytes(tokenBytes);
		
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(tokenBytes);
	}
	
	// 토큰 원문을 DB에 저장할 해시로 변환하는 메서드
	private String hashSignupVerificationToken(String token) {
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			byte[] hash = sha256.digest(token.getBytes(StandardCharsets.UTF_8));
			
			return HexFormat.of().formatHex(hash);
			
			// NoSuchAlgorithmException : 암호화 알고리즘을 찾을 수 없을때 예외
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
		}
	}

	private String createSecretHash(String email, String code) {
		
		return EmailVerificationCodeHasher.hash(email + "\n" + code);
	}

	// 회원가입 시 회원정보
	@Transactional
	public long signup(
			String name, LocalDate birthDate, String rawEmail, 
			String signupVerificationToken) {
		
		if(name == null || name.isBlank() || birthDate == null) {
			throw new IllegalArgumentException("이름과 생년월일을 입력해주세요.");
		}
		
		if(signupVerificationToken == null || signupVerificationToken.isBlank()) {
			throw new InvalidSignupEmailVerificationException();
		}
			
		String email = normalizeAndValidateEmail(rawEmail);
		String tokenHash = hashSignupVerificationToken(signupVerificationToken);
		
		int consumedRows = signupVerificationTokenMapper.consumeToken(tokenHash, email);
		
		if(consumedRows != 1) {
			throw new InvalidSignupEmailVerificationException();
		}
			
		NewUser user = new NewUser(email, name, birthDate);
		int insertRows = userMapper.insertUser(user);
		
		if(insertRows == 0) {
			throw new DuplicateSignupEmailException();
		}
		
		if(insertRows != 1 || user.getId() == null) {
			throw new IllegalStateException("회원 저장에 실패했습니다.");
		}
		
		return user.getId();
		
	}
}
