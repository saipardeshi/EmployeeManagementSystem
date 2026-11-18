package com.mainapp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {

	@Id
	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@Column(name = "password", nullable = false)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // accepted on signup/signin, never exposed in JSON responses
	private String password;

	@Column(name = "email")
	private String email;

	@Column(name = "role")
	private String role; // "ROLE_ADMIN" or "ROLE_USER"

	public User() {
	}

	public User(String username, String password, String email, String role) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
	}

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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", email=" + email + ", role=" + role + "]";
	}
}
