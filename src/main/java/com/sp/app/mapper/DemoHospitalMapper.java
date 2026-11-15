package com.sp.app.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Profile;

import com.sp.app.domain.dto.DemoHospitalDto;

@Profile("demo")
@Mapper
public interface DemoHospitalMapper {

	Optional<DemoHospitalDto> findHospital(@Param("id") long id);
}
