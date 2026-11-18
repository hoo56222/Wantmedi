package com.sp.app;

import java.util.OptionalLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.LoginRequest;
import com.sp.app.service.LoginService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class LoginController {

	private final LoginService loginService;
	
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }
    
    @PostMapping("/api/login")
    public ResponseEntity<Void> login(
    		@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    	
    		OptionalLong userId = loginService.authenticate(request.email(), request.password());
    		
    		// 인증에 성공하지 않음
    		if(userId.isEmpty()) {
    			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    		}
    		
    		// 현재 요청 세션 가져오기
    		HttpSession session = servletRequest.getSession();
    		// 로그인 성공 직후 세션 ID 교체
    		servletRequest.changeSessionId();
    		// 서버 세션에 저장
    		session.setAttribute("userId", userId.getAsLong());
    		
    		return ResponseEntity.noContent().build();
    	
    }
}
