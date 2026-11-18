package com.sp.app.service;

import java.util.OptionalLong;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.LoginUserRow;
import com.sp.app.mapper.UserMapper;

@Service
public class LoginService {
	
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;
    
    public LoginService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }
    
    public OptionalLong authenticate(String rawEmail, String password) {
    		if(password == null || password.isBlank()) {
    			return OptionalLong.empty();
    		}
    		
    		String email;
    		
    		try {
			email = EmailAddressNormalizer.normalizeAndValidate(rawEmail);
		} catch (InvalidSignupEmailException  e) {
			return OptionalLong.empty();
		}
    		
    		LoginUserRow user = userMapper.findLoginUserByEmail(email);
    		String hash = user == null ? dummyPasswordHash : user.passwordHash();
    		// 입력한 비밀번호와 저장된 해시가 일치하는지 비교
    		boolean matches = passwordEncoder.matches(password, hash);
    		
    		// 실패 시 빈 OptionalLong 을 반환
    		if(user == null || !matches) {
    			return OptionalLong.empty();
    		}
    		
    		return OptionalLong.of(user.id());
    }
}
