package com.sp.app.mapper;

import java.time.OffsetDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 

@Mapper
public interface SignupVerificationTokenMapper {

	public int insertToken(
			@Param("tokenHash") String tokenHash,
			@Param("email") String email,
			@Param("expiresAt") OffsetDateTime expiresAt);
}
