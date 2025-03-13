package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.LoginDto;
import com.example.demo.entities.User;
import com.example.demo.services.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> authUser(@RequestBody LoginDto logindto, HttpServletResponse response) {
        try {
            String username = logindto.getUsername();
            String password = logindto.getPassword();
            User user = loginService.authenticateUser(username, password);
            String token = loginService.generateToken(user);

            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Set to true for HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(3600);
            response.addCookie(cookie);
            response.setHeader("Set-Cookie",
                    String.format("authToken=%s; HttpOnly; Path=/; Max-Age=3600; Secure; SameSite=None", token));

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login Success");
            responseBody.put("role", user.getRole().name());
            responseBody.put("username", user.getUsername());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            log.error("Login failed for user: " + logindto.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String,String>> logout(HttpServletResponse response,HttpServletRequest request) {
    	try {
    		User user = (User) request.getAttribute("authenticatedUser");
        	loginService.logOut(user);
        	Cookie cookie = new Cookie("authToken", null);
        	cookie.setHttpOnly(true);
        	cookie.setMaxAge(0);
        	cookie.setPath("/");
        	response.addCookie(cookie);
        	Map<String,String> responseBody = new HashMap<>();
        	responseBody.put("message", "Logout Successful");
        	return ResponseEntity.ok(responseBody);
		} catch (RuntimeException e) {
			Map<String,String> errorResponse = new HashMap<>();
			errorResponse.put("message","Logout failed");
			return ResponseEntity.status(500).body(errorResponse);
		}
    	
    	}
}
