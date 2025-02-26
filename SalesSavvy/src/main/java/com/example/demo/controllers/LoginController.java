package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.LoginDto;
import com.example.demo.entities.User;
import com.example.demo.services.LoginService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/api/user")
public class LoginController {
	LoginService loginService;
	public LoginController(LoginService loginService) {
		this.loginService = loginService;
	}
	@PostMapping("/login")
	public ResponseEntity<?> authUser(@RequestBody LoginDto logindto, HttpServletResponse response) {
		try {
			String username = logindto.getUsername();
			String password = logindto.getPassword();
			User user = loginService.authenticate(username, password);
			String token = loginService.generateToken(user);
			
			Cookie cookie = new Cookie("authToken", token);
			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(3600);
			cookie.setDomain("localhost");
			response.addCookie(cookie);
			response.setHeader("set-cookie", 
					String.format("authToken=%s; HttpOnly ; path=/ ; Max-Age=3600 ; sameSite=none", token));
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("message", "Login Success");
			responseBody.put("role", user.getRole().name());
			responseBody.put("username", user.getUsername());
			
			return ResponseEntity.ok(responseBody);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
			//return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
}
