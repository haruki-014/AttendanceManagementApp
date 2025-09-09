package com.example.attendance.dto;

import java.time.LocalDateTime;

public class Attendance {

	private int id;
	private String userId;
	private LocalDateTime checkInTime;
	private LocalDateTime checkOutTime;
	
	public String getUserId() {
		return userId;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Attendance(String userId) {
		this.userId = userId;
	}
	
	public LocalDateTime getCheckInTime() {
		return checkInTime;
	}
	
	public void setCheckInTime(LocalDateTime checkInTime) {
		this.checkInTime = checkInTime;
	}
	
	public LocalDateTime getCheckOutTime() {
		return checkOutTime;
	}
	
	public void setCheckOutTime(LocalDateTime checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

}
