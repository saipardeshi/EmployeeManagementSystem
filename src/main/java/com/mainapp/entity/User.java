// package com.mainapp.entity;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;

// @Entity
// @Table
// public class User {
// @Id
// @Column
//  String username;
// @Column
//  String password;
// @Column
//  String email;
// public String getUsername() {
// 	return username;
// }
// public void setUsername(String username) {
// 	this.username = username;
// }
// public String getPassword() {
// 	return password;
// }
// public void setPassword(String password) {
// 	this.password = password;
// }
// public String getEmail() {
// 	return email;
// }
// public void setEmail(String email) {
// 	this.email = email;
// }
// @Override
// public String toString() {
// 	return "user [username=" + username + ", password=" + password + ", email=" + email + "]";
// }
 

 
// }
package com.mainapp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class User {
@Id
@Column
 String username;
@Column
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // accepted on signup, never sent back out
 String password;
@Column
 String email;
public String getUsername() {
	return username;
}
public void setUsername(String username) {
	this.username = username;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
@Override
public String toString() {
	return "user [username=" + username + ", password=" + password + ", email=" + email + "]";
}
}