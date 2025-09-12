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
	
	private String userName;
	private String password;
	private String role;
	private boolean isEnabled;
	
	public User(String userName, String password, String role) {
		this(userName, password, role, true);
	}
	
<<<<<<< HEAD
	public User(String userName, String password, String role, boolean isEnabled) {
		this.userName = userName;
=======
	/* ユーザー作成時 */
	public User(String name, String password, String role, boolean isEnabled) {
		this.name = name;
>>>>>>> branch 'feature/connectDB-attendance' of ssh://git@github.com/haruki-014/AttendanceManagementApp.git
		this.password = password;
		this.role = role;
		this.isEnabled = isEnabled;
	}
	
<<<<<<< HEAD
	public String getUserName() {
		return userName;
=======
	/* ログイン時 */
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
>>>>>>> branch 'feature/connectDB-attendance' of ssh://git@github.com/haruki-014/AttendanceManagementApp.git
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
