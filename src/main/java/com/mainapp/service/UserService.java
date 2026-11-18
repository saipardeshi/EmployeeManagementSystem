package com.mainapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mainapp.entity.User;
import com.mainapp.respository.UserRespository;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {

	@Autowired
	private UserRespository uRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostConstruct
	public void initDefaultAccounts() {
		// Ensure default Admin exists with ROLE_ADMIN
		Optional<User> adminOpt = uRepo.findById("admin");
		if (adminOpt.isEmpty()) {
			User admin = new User("admin", passwordEncoder.encode("123"), "admin@staffsync.com", "ROLE_ADMIN");
			uRepo.save(admin);
		} else {
			User admin = adminOpt.get();
			if (admin.getRole() == null || !"ROLE_ADMIN".equals(admin.getRole())) {
				admin.setRole("ROLE_ADMIN");
				uRepo.save(admin);
			}
		}

		// Ensure default Normal User exists with ROLE_USER
		Optional<User> userOpt = uRepo.findById("user");
		if (userOpt.isEmpty()) {
			User normalUser = new User("user", passwordEncoder.encode("123"), "user@staffsync.com", "ROLE_USER");
			uRepo.save(normalUser);
		} else {
			User normalUser = userOpt.get();
			if (normalUser.getRole() == null || !"ROLE_USER".equals(normalUser.getRole())) {
				normalUser.setRole("ROLE_USER");
				uRepo.save(normalUser);
			}
		}

		// Update existing user 'abhi' to ROLE_USER if role is null
		Optional<User> abhiOpt = uRepo.findById("abhi");
		if (abhiOpt.isPresent()) {
			User abhi = abhiOpt.get();
			if (abhi.getRole() == null) {
				abhi.setRole("ROLE_USER");
				uRepo.save(abhi);
			}
		}
	}

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

		// Assign default role: username 'admin' gets ROLE_ADMIN, all other signups get ROLE_USER
		if (u.getRole() == null || u.getRole().isBlank()) {
			u.setRole("admin".equalsIgnoreCase(username) ? "ROLE_ADMIN" : "ROLE_USER");
		} else {
			String r = u.getRole().trim().toUpperCase();
			if (!r.startsWith("ROLE_")) {
				r = "ROLE_" + r;
			}
			u.setRole(r);
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
