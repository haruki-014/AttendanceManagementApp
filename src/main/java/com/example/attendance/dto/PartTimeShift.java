package com.example.attendance.dto;

import java.time.LocalTime;

public class PartTimeShift {
    private Integer id;
    private Integer userId;      // アルバイトのユーザーID
    private int dayOfWeek;       // 0=日曜, 1=月曜, ... 6=土曜
    private LocalTime startTime; // 勤務開始時刻
    private LocalTime endTime;   // 勤務終了時刻

    // --- コンストラクタ ---
    public PartTimeShift() {}

    public PartTimeShift(Integer id, Integer userId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.userId = userId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- getter/setter ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    // --- 表示用メソッド ---
    public String getDayOfWeekName() {
        String[] days = {"日曜","月曜","火曜","水曜","木曜","金曜","土曜"};
        if (dayOfWeek >= 0 && dayOfWeek < days.length) {
            return days[dayOfWeek];
        }
        return "不明";
    }
}
