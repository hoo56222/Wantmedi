package com.sp.app.domain.dto;

import java.time.OffsetDateTime;

public record SignupEmailVerificationRow(
		long verificationId,
		String secretHash,
		OffsetDateTime expiresAt,
		OffsetDateTime usedAt) {

}
