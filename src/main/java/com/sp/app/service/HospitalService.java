package com.sp.app.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

import com.sp.app.domain.dto.HospitalDto;
import com.sp.app.domain.dto.HospitalListResponse;

import tools.jackson.databind.JsonNode;

@Service
public class HospitalService {

	private final RestClient restClient;
	private final String serviceKey;
	
	// RestClient.Builder : 기본 주소를 지정해 HTTP 요청 도구 만듦
	public HospitalService(
			RestClient.Builder restClientBuilder,
			@Value("${hira.hospital.base-url}") String baseUrl,
			@Value("${hira.service-key}") String serviceKey) {
		
		this.restClient = restClientBuilder
				.baseUrl(baseUrl)
				.build();
		
		// 인증키 인코딩 한번 풀기
		try {
			this.serviceKey = UriUtils.decode(serviceKey, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("병원 API 인증키 설정 형식이 올바르지 않습니다.");
		}
	}
	
	// 공공 API 요청 메서드
	// JsonNode : JSON 데이터를 트리로 다루기 위한 클래스
	// 시도, 시군구, 페이지 번호, 한 페이지 결과 수
	private JsonNode requestHospitals(
			String sidoCd, String sgguCd,
			int pageNo, int numOfRows) {
		
		// -> : Java 의 람다식
		// uriBuilder 를 받아서 경로와 조회 조건을 붙이고, 완성된 주소를 반환
		try {
			JsonNode response = restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/getHospBasisList")
							.queryParam("ServiceKey", "{serviceKey}")
							.queryParam("sidoCd", sidoCd)
							.queryParam("sgguCd", sgguCd)
							.queryParam("pageNo", pageNo)
							.queryParam("numOfRows", numOfRows)
							.queryParam("_type", "json")
							.build(serviceKey))
					.retrieve()
					.body(JsonNode.class);
			
			if(response == null) {
				throw new IllegalStateException("병원 조회 응답에 실패 했습니다.");
			}
			
			return response;
		} catch (RestClientException e) {
		    System.err.println("병원 API 오류 종류: "
		            + e.getClass().getSimpleName());

		    System.err.println("병원 API 원인 종류: "
		            + e.getMostSpecificCause().getClass().getSimpleName());

		    if (e instanceof RestClientResponseException httpError) {
		        System.err.println("병원 API HTTP 상태: "
		                + httpError.getStatusCode().value());
		    }

		    throw new IllegalStateException("병원 정보를 가져오지 못했습니다.");
		}
	}
	
	// 병원 한 건을 HospitalDto 로 변환하는 메소드
	// 공공 API 에서 받은 병원 정보를 앱에서 사용할 형태로 처리하기 위함
	private HospitalDto toHospitalDto(JsonNode item) {
		String id = item.path("ykiho").asString(""); // 암호화된 요양기호
		String name = item.path("yadmNm").asString(""); // 병원명
		String address = item.path("addr").asString(""); // 주소
		
		if(id.isBlank() || name.isBlank()) {
			throw new IllegalStateException("병원 식별값 또는 병원명이 없습니다.");
		}
		
		return new HospitalDto(id, name, address);
	}
	
	// 
	public HospitalListResponse findHospitals(
			String sidoCd, String sgguCd,
			int pageNo, int numOfRows) {
		
		// numOfRows : 한 페이지 결과 수
		if(sidoCd == null || !sidoCd.matches("[0-9]{6}")
				|| sgguCd == null || !sgguCd.matches("[0-9]{6}")
				|| pageNo < 1 || numOfRows < 1 || numOfRows > 100) {
			throw new IllegalArgumentException("지역코드 또는 페이지 조건이 올바르지 않습니다.");
		}
		// 공공 API 호출, 응답에서 response 내용 꺼내기
		JsonNode response = requestHospitals(sidoCd, sgguCd, pageNo, numOfRows).path("response");
		// response -> header -> resultCode 순서로 문자열 읽기
		// asString() : 값이 없거나 문자열로 변환할 수 없을 때 사용
		String resultCode = response.path("header")
				.path("resultCode").asString("");
		
		if(!"00".equals(resultCode)) {
			throw new IllegalStateException("공공 API 병원 조회에 실패했습니다.");
		}
		
		// 정상적인 페이지 번호와 요청 건수는 1이상, 전체 결과수는 0이상이므로
		// 값이 없거나 숫자로 변환할 수 없을 때 사용할 기본값
		JsonNode body = response.path("body");
		int responsePageNo = body.path("pageNo").asInt(-1);
		int responseNumOfRows = body.path("numOfRows").asInt(-1);
		long totalCount = body.path("totalCount").asLong(-1);
		
		if(responsePageNo != pageNo || responseNumOfRows != numOfRows || totalCount < 0) {
			throw new IllegalStateException("병원 조회 페이지 정보가 올바르지 않습니다.");
		}
		
		// 병원 목록
		List<HospitalDto> hospitals = new ArrayList<>();
		// 실제 병원 데이터가 들어있는 부분을 꺼냄
		// body -> items -> item
		JsonNode item = body.path("items").path("item");
		
		// 병원 데이터가 배열이면
		if(item.isArray()) {
			// 한건씩 꺼내
			for(JsonNode hospital : item) {
				// dto 로 바꿔 목록에 추가
				hospitals.add(toHospitalDto(hospital));
			}
		// 값이 {...} 형태의 JSON 객체인지 확인
		} else if(item.isObject()) {
			// 병원 한건이 객체로 왔으면 dto 로 바꿔 목록에 추가
			hospitals.add(toHospitalDto(item));
		// 객체 및 배열도 아니고 위의 빈 값에도 해당하지 않으면 오류를 던짐
		} else if(!(item.isMissingNode() || item.isNull()
				|| (item.isString() && item.asString("").isBlank()))) {
			throw new IllegalStateException("병원 목록 응답 형식이 올바르지 않습니다.");
		}
		
		// offset : 현재 페이지 앞에서 건너뛴 병원 수
		long offset = ((long) pageNo - 1) * numOfRows;
		// expectedCount : 현재 페이지에 있는 병원 수
		// Math.max : 마지막 페이지를 넘어가도 음수가 되지 않도록 최소 0으로 제한
		// Math.min : 한 페이지의 요청 건수를 넘지 않도록 제한
		long expectedCount = Math.min(numOfRows, Math.max(0L, totalCount - offset));
		
		if(hospitals.size() != expectedCount) {
			throw new IllegalStateException("병원 목록과 페이지 정보가 일치하지 않습니다.");
		}
		
		return new HospitalListResponse(
				List.copyOf(hospitals),
				responsePageNo,
				responseNumOfRows,
				totalCount);
	}
}
