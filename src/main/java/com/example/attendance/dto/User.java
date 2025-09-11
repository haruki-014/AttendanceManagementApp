package com.example.attendance.dto;

public class User {
	
	private int id;
	private String name;
	private String password;
	private String role;
	private boolean isEnabled;
	
	public User(String name, String password, String role) {
		this(name, password, role, true);
	}
	
	public User(String name, String password, String role, boolean isEnabled) {
		this.name = name;
		this.password = password;
		this.role = role;
		this.isEnabled = isEnabled;
	}
	
	public User(int id, String name, String password, String role, boolean isEnabled) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.role = role;
		this.isEnabled = isEnabled;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
