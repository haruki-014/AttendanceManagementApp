package com.example.attendance.dto;

public class User {
	
	private Integer id;
	private String name;
	private String password;
	private String role;
	private Boolean isEnabled;
	
	public User(String name, String password, String role) {
		this(null, name, password, role, true);
	}
	
	public User(Integer id, String name, String password, String role, boolean isEnabled) {
		this.id = id;
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
	
	public Boolean isEnabled() {
		return isEnabled;
	}
	
	public boolean getEnabled() {
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
	
	public void setEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
