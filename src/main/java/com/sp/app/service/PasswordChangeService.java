package com.sp.app.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.LoginUserRow;
import com.sp.app.mapper.UserMapper;

@Service
public class PasswordChangeService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    public PasswordChangeService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    } 
    
    // 비밀번호 변경 메소드
    public boolean changePassword(
    		long userId, String currentPassword, String newPassword) {
    	
    		if(currentPassword == null || currentPassword.isBlank()
    				|| newPassword == null || newPassword.isBlank()) {
    			return false;
    		}
    		
    		// 조회할 회원 ID 
    		LoginUserRow user = userMapper.findLoginUserById(userId);
    		
    		if(user == null || !passwordEncoder.matches(currentPassword, user.passwordHash())) {
    			return false;
    		}
    		
    		String newPasswordHash = passwordEncoder.encode(newPassword);
    		
    		// 비밀번호 해시를 변경, 메서드가 반환하는 변경 행수를 1과 비교
    		return userMapper.changePasswordHash(userId, user.passwordHash(), newPasswordHash) == 1;
    }
}
