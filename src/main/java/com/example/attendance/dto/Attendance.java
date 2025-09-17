package com.example.attendance.dto;

import java.time.LocalDateTime;

public class Attendance {

	private int id;
	private Integer userId;
	private LocalDateTime checkInTime;
	private LocalDateTime checkOutTime;
	
	
	public Attendance() {
		
	}

	public Attendance(Integer userId) {
		this.userId = userId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
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
