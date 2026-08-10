// package com.mainapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mainapp.entity.User;
import com.mainapp.respository.UserRespository;

@Service
public class UserService {

	@Autowired
	UserRespository uRepo;

	@Autowired
	PasswordEncoder passwordEncoder;

	/** Returns true if the account was created, false if the username was already taken. */
	public boolean addUser(User u) {
		Optional<User> existing = uRepo.findById(u.getUsername());
		if (existing.isPresent()) {
			return false;
		}
		u.setPassword(passwordEncoder.encode(u.getPassword()));
		uRepo.save(u);
		return true;
	}

	/** Returns the user if credentials match, otherwise empty. */
	public Optional<User> authenticate(String username, String rawPassword) {
		return uRepo.findById(username)
				.filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()));
	}

	public Optional<User> getUser(String name) {
		return uRepo.findById(name);
	}
}