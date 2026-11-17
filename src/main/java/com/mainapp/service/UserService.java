package com.mainapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mainapp.entity.User;
import com.mainapp.respository.UserRespository;

@Service
public class UserService {

	@Autowired
	private UserRespository uRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/** Returns true if the account was created, false if username exists or input is invalid. */
	public boolean addUser(User u) {
		if (u == null || u.getUsername() == null || u.getUsername().trim().isEmpty()
				|| u.getPassword() == null || u.getPassword().trim().isEmpty()) {
			return false;
		}
		String username = u.getUsername().trim();
		if (uRepo.existsById(username)) {
			return false;
		}
		u.setUsername(username);
		if (u.getEmail() != null) {
			u.setEmail(u.getEmail().trim());
		}
		u.setPassword(passwordEncoder.encode(u.getPassword().trim()));
		uRepo.save(u);
		return true;
	}

	/** Returns the user if credentials match, otherwise empty. */
	public Optional<User> authenticate(String username, String rawPassword) {
		if (username == null || rawPassword == null || username.isBlank() || rawPassword.isBlank()) {
			return Optional.empty();
		}
		return uRepo.findById(username.trim())
				.filter(u -> {
					if (u.getPassword() == null) {
						return false;
					}
					// If already BCrypt encoded
					if (u.getPassword().startsWith("$2a$") || u.getPassword().startsWith("$2b$") || u.getPassword().startsWith("$2y$")) {
						return passwordEncoder.matches(rawPassword, u.getPassword());
					}
					// Legacy plain-text fallback with auto-migration to BCrypt
					if (u.getPassword().equals(rawPassword)) {
						u.setPassword(passwordEncoder.encode(rawPassword));
						uRepo.save(u);
						return true;
					}
					return false;
				});
	}

	public Optional<User> getUser(String name) {
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		return uRepo.findById(name.trim());
	}
}
