package com.sp.app.mapper;

import java.time.OffsetDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.PasswordSetupEmailVerificationRow;

@Mapper
public interface PasswordSetupEmailVerificationMapper {

	// 첫 비밀번호 설정용 인증 기록 저장
	public int insertVerification(
			@Param("userId") long userId,
			@Param("secretHash") String secretHash,
			@Param("expiresAt") OffsetDateTime expiresAt);
	
	// 회원 ID로 최근에 발급한 인증 기록 한 건 조회
	public PasswordSetupEmailVerificationRow findLatestByUserId(
			@Param("userId") long userId);
	
	// 인증번호 오류 시 실패 횟수 1카운트
	public int incrementFailedAttempts(
			@Param("verificationId") long verificationId,
			@Param("maxFailedAttempts") int maxFailedAttempts);
	
	// 인증번호가 맞을 때 인증 기록 한번만 사용
	public int markUsed(
			@Param("verificationId") long verificationId,
			@Param("maxFailedAttempts") int maxFailedAttempts);
}
