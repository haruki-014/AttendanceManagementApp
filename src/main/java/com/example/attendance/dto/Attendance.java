package com.example.attendance.dto;

import java.time.LocalDateTime;

public class Attendance {

	private int id;
	private Integer userId;
	private LocalDateTime check_in_time;
	private LocalDateTime check_out_time;
	
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Attendance() {
		
	}

	public Attendance(Integer userId) {
		this.userId = userId;
	}

	public LocalDateTime getCheck_in_time() {
		return check_in_time;
	}

	public void setCheck_in_time(LocalDateTime check_in_time) {
		this.check_in_time = check_in_time;
	}

	public LocalDateTime getCheck_out_time() {
		return check_out_time;
	}

	public void setCheck_out_time(LocalDateTime check_out_time) {
		this.check_out_time = check_out_time;
	}
	
}
