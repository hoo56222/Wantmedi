package com.sp.app.domain.dto;

import java.util.List;

public record HospitalListResponse(
		List<HospitalDto> items,
		int pageNo,
		int numOfRows,
		long totalCount) {

}
