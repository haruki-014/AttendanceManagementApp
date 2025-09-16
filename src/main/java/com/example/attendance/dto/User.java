package com.example.attendance.dto;

public class User {
	
	private Integer id;
	private String name;
	private String password;
	private String role;
	private boolean isEnabled;
	
	/* 初期作成時 */
	public User(String name, String password, String role) {
		this(name, password, role, true);
	}
	
	public User(String name, String password, String role, boolean isEnabled) {
		this.name = name;
		this.password = password;
		this.role = role;
		this.isEnabled = isEnabled;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getRole() {
		return role;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
