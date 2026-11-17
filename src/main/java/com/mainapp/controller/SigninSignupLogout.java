package com.mainapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mainapp.entity.User;
import com.mainapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class SigninSignupLogout {

	@Autowired
	private UserService uService;

	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> signup(@RequestBody(required = false) User u) {
		if (u == null || u.getUsername() == null || u.getUsername().trim().isEmpty()
				|| u.getPassword() == null || u.getPassword().trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "Username and password cannot be empty"));
		}

		boolean created = uService.addUser(u);
		if (created) {
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("message", "Account created successfully"));
		}
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(Map.of("message", "Username already taken"));
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody(required = false) User credentials, HttpServletRequest req) {
		if (credentials == null || credentials.getUsername() == null || credentials.getPassword() == null
				|| credentials.getUsername().trim().isEmpty() || credentials.getPassword().trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", "Username and password are required"));
		}

		return uService.authenticate(credentials.getUsername().trim(), credentials.getPassword().trim())
				.map(user -> {
					HttpSession session = req.getSession(true);
					session.setAttribute("username", user.getUsername());
					return ResponseEntity.ok(Map.of(
							"username", user.getUsername(),
							"email", user.getEmail() != null ? user.getEmail() : ""));
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("message", "Invalid username or password")));
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
	}

	// Frontend session check: verifies if session is active on page load/refresh
	@GetMapping("/me")
	public ResponseEntity<?> me(HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if (session != null && session.getAttribute("username") != null) {
			String username = (String) session.getAttribute("username");
			return uService.getUser(username)
					.map(user -> ResponseEntity.ok(Map.of(
							"username", user.getUsername(),
							"email", user.getEmail() != null ? user.getEmail() : "")))
					.orElseGet(() -> ResponseEntity.ok(Map.of("username", username)));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not signed in"));
	}
}
