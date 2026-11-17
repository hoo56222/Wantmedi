package com.sp.app.domain.dto;

import java.time.LocalDate;

public record SignupRequest(
		String name, LocalDate birthDate,
		String email, String signupVerificationToken) {

}
