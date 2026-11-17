package com.sp.app.mapper;

import java.time.OffsetDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.SignupEmailVerificationRow;

// OffsetDateTime : 인증값이 정확히 언제 만료되는지 표현하기 위함
@Mapper
public interface SignupEmailVerificationMapper {

	public int insertVerification(
			@Param("email") String email,
			@Param("secretHash") String secretHash,
			@Param("expiresAt") OffsetDateTime expiresAt);
	
	// 가장 최근 발송된 인증번호
	public SignupEmailVerificationRow findLatestByEmail(
			@Param("email") String email);
	
	// 검증에 성공한 인증번호를 한번만 사용하도록 처리
	public int markUsed(@Param("verificationId") long verificationId);
}
