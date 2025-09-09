package com.example.attendance.dto;

public class User {
	
	private String userName;
	private String password;
	private String role;
	private boolean isEnabled;
	
	public User(String userName, String password, String role) {
		this(userName, password, role, true);
	}
	
	public User(String userName, String password, String role, boolean isEnabled) {
		this.userName = userName;
		this.password = password;
		this.role = role;
		this.isEnabled = isEnabled;
	}
	
	public String getUserName() {
		return userName;
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
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
