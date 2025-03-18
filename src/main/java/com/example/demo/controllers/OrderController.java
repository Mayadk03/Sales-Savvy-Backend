package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.User;
import com.example.demo.services.OrderService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5175", allowCredentials = "true")
@RequestMapping("/api/orders")
public class OrderController {

	private OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> getOrdersForUsers(HttpServletRequest request) {
		
		try {
			User user = (User) request.getAttribute("authenticatedUser");
			
			if(user == null) {
				return ResponseEntity.status(401).body(Map.of("error", "User Not Authenticated"));
			}
			
			Map<String, Object> response =  orderService.getOrderForUser(user);
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}
}
