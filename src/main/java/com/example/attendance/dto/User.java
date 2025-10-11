package com.example.attendance.dto;

/* 
 attendance_db=# \d users;
                                      Table "public.users"
   Column   |          Type          | Collation | Nullable |              Default              
------------+------------------------+-----------+----------+-----------------------------------
 id         | integer                |           | not null | nextval('users_id_seq'::regclass)
 name       | character varying(50)  |           | not null | 
 password   | character varying(255) |           | not null | 
 role       | character varying(20)  |           | not null | 
 is_enabled | boolean                |           |          | true
Indexes:
    "user_id" PRIMARY KEY, btree (id)
Referenced by:
    TABLE "attendance" CONSTRAINT "attendance_user_id_fkey" FOREIGN KEY (user_id) REFERENCES users(id)
 */

public class User {
	

	private Integer id;
	private String name;
	private String password;
	private String role;
	private Boolean isEnabled;
	private Position position; // ← 追加（役職：OFFICER, COMPANY_EMPLOYEE, PART_TIME）

	public User(String name, String password, String role, Position position) {
		this(null, name, password, role, true, position);
	}
	
	public User(Integer id, String name, String password, String role, boolean isEnabled, Position position) {

		this.id = id;
		this.name = name;
		this.password = password;
		this.role = role;
		this.isEnabled = isEnabled;
		this.position = position;
	}
	
	// --- getter ---
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
	
	public Position getPosition() {
		return position;
	}
	
	// --- setter ---
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

	public void setPosition(Position position) {
		this.position = position;
	}
}
