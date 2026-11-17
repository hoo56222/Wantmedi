package com.sp.app.domain;

import java.time.LocalDate;

public class NewUser {
	private Long id;
	private final String email;
	private final String name;
	private final LocalDate birthDate;
	
	public NewUser(String email, String name, LocalDate birthDate) {
		this.email = email;
		this.name = name;
		this.birthDate = birthDate;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

}
