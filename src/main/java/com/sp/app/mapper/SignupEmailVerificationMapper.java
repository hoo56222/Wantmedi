package com.sp.app.mapper;

import java.time.OffsetDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// OffsetDateTime : 인증값이 정확히 언제 만료되는지 표현하기 위함
@Mapper
public interface SignupEmailVerificationMapper {

	public int insertVerification(
			@Param("email") String email,
			@Param("secretHash") String secretHash,
			@Param("expiresAt") OffsetDateTime expiresAt);
}
