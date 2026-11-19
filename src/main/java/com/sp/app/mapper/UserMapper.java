package com.sp.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.NewUser;
import com.sp.app.domain.dto.LoginUserRow;

@Mapper
public interface UserMapper {

	public int insertUser(NewUser user);
	
	// 이메일로 가입한 회원
	public Long findUserIdByEmail(@Param("email") String email);
	
	// 첫 비밀번호 해시를 저장
	public int setInitialPasswordHash(
			@Param("userId") long userId,
			@Param("passwordHash") String passwordHash);
	
	// 첫 비밀번호 설정 대상 회원 조회
	public Long findUserIdWithoutPasswordByEmail(
			@Param("email") String email);
	
	// 로그인
	public LoginUserRow findLoginUserByEmail(@Param("email") String email);

	// 회원 비밀번호 변경 시 필요한 정보 조회
	public LoginUserRow findLoginUserById(@Param("userId") long userId);

	// 비밀번호 해시 변경 메소드
	public int changePasswordHash(
			@Param("userId") long userId,
			@Param("expectedPasswordHash") String expectedPasswordHash,
			@Param("newPasswordHash") String newPasswordHash);
}
