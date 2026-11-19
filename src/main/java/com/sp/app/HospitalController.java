package com.sp.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.HospitalListResponse;
import com.sp.app.service.HospitalService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class HospitalController {

	private final HospitalService hospitalService;
	
	public HospitalController(
			HospitalService hospitalService) {
		this.hospitalService = hospitalService;
	}
	
	// 병원 목록 조회
	@GetMapping("/api/hospitals")
	public ResponseEntity<HospitalListResponse> findHospitals(
			@RequestParam(name = "sidoCd", required = false) String sidoCd,
			@RequestParam(name = "sgguCd", required = false) String sgguCd,
			@RequestParam(name = "pageNo", defaultValue = "1") String pageNo,
			@RequestParam(name = "numOfRows", defaultValue = "10") String numOfRows,
			HttpServletRequest servletRequest) {
		
		// 로그인 세션 확인
		// 기존 세션이 없으면 새로 만들지 말라. 즉 조회만 하는 것.
		HttpSession session = servletRequest.getSession(false);
		
		if(session == null || !(session.getAttribute("userId") instanceof Long)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		try {
			HospitalListResponse response = hospitalService.findHospitals(
					sidoCd, sgguCd, 
					Integer.parseInt(pageNo), Integer.parseInt(numOfRows));
		
			return ResponseEntity.ok(response);
		
			// 잘못된 메서드 혹은 인자값
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
			// 객체의 현재 상태가 해당 메서드를 호출하기에 부적합할 때 발생하는 런타임 예외
//		} catch (IllegalStateException e) {
//			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		} catch (IllegalStateException e) {
		    StackTraceElement location = e.getStackTrace()[0];

		    System.err.println("병원 조회 실패 위치: "
		            + location.getClassName() + "."
		            + location.getMethodName() + ":"
		            + location.getLineNumber());

		    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
		}
	}
}
