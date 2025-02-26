package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;

@Service
public class RegistrationService {
	private final UserRepository userrepository;
	private final BCryptPasswordEncoder passwordEncoder;
	@Autowired
	public RegistrationService(UserRepository userrepository) {
		this.userrepository = userrepository;
		passwordEncoder = new BCryptPasswordEncoder();
	}
public User userRegitser(User user) {
	if(userrepository.findByUsername(user.getUsername()).isPresent()) {
		throw new RuntimeException("Username with "+user.getUsername()+" is already present");
	}
	if(userrepository.findByEmail(user.getEmail()).isPresent()) {
		throw new RuntimeException("user with same email is aready registered");
	}
	user.setPassword(passwordEncoder.encode(user.getPassword()));
	return userrepository.save(user);
}
}
