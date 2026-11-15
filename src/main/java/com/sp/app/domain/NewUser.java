package com.sp.app.domain;

public class NewUser {
	private Long id;
	private final String email;
	private final String passwordHash;
	
	public NewUser(String email, String passwordHash) {
		this.email = email;
		this.passwordHash = passwordHash;
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
	
	public String getPasswordHash() {
		return passwordHash;
	}
}
