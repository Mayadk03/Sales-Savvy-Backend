package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.User;
import com.example.demo.services.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5175", allowCredentials = "true")
@RequestMapping("/api/user")
public class RegisterationController {
	private UserService userService;
	public RegisterationController(UserService userService) {
		this.userService= userService;
	}
	
	//ResponseEntity--> inbuilt act as hhtp status code(200,400 etc)
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		try {
			return ResponseEntity.ok().body(Map.of("message", "Regitsred Succesfully", "User", userService.registerUser(user)));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
