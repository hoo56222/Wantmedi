package com.sp.app.domain.dto;

import java.time.OffsetDateTime;

public record PasswordSetupEmailVerificationRow(
		long verificationId,
		String secretHash,
		int failedAttempts,
		OffsetDateTime expiresAt,
		OffsetDateTime usedAt) {

}
