package com.example.attendance.dto;

public enum Position {
	
	OFFICER("役員"),
	COMPANY_EMPLOYEE("社員"),
	PART_TIME("アルバイト");
	
	private final String displayName;
	
	Position(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

}
