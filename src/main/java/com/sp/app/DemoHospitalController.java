package com.sp.app;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.DemoHospitalDto;
import com.sp.app.mapper.DemoHospitalMapper;

@Profile("demo")
@RestController
public class DemoHospitalController {

	private final DemoHospitalMapper hospitalMapper;

	public DemoHospitalController(DemoHospitalMapper hospitalMapper) {
		this.hospitalMapper = hospitalMapper;
	}

	@GetMapping("/api/hospitals/{id}")
	public ResponseEntity<DemoHospitalDto> findHospital(@PathVariable("id") long id) {
		return ResponseEntity.of(hospitalMapper.findHospital(id));
	}
}
