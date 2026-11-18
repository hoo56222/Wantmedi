package com.sp.app.domain.dto;

public record LoginUserRow(
		long id, String passwordHash) {

	@Override
	public String toString() {
		return "LoginUserRow[id=" + id + "]";
	}
}
