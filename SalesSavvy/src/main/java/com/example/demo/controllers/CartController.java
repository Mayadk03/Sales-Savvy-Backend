package com.example.demo.controllers;

import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.CartService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {
	
	private UserRepository userRepository;
	
	private CartService cartService;
	
	public CartController(UserRepository userRepository, CartService cartItemService) {
		this.cartService = cartItemService;
		this.userRepository = userRepository;
	}
	
	@GetMapping("/items/count")
	public ResponseEntity<Integer> getCartCount(@RequestParam String username) {
		Optional<User> opuser = userRepository.findByUsername(username);
		int count = 0;
		if(opuser.isEmpty()) {
			
			return ResponseEntity.ok(0);
		}
			User user = opuser.get();
			count = cartService.getCartItemCount(user.getUserId());
		
		return ResponseEntity.ok(count);
	}
	
	@GetMapping("/items")
	public ResponseEntity<Map<String,Object>> getCartItem(HttpServletRequest request) {
		User user = (User) request.getAttribute("authenticatedUser");
		
		Map<String, Object>  response = cartService.getCartItems(user.getUserId());
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/add")
	public ResponseEntity<Void> addToCart(@RequestBody Map<String,Object> request) {
		
		String username = (String) request.get("username");
		int productId = (int) request.get("productId");
		
		User user = userRepository.findByUsername(username).orElse(null);
		cartService.addToCart(user.getUserId(), productId, 1);
		
		
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@PutMapping("/update")
	public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request) {
		String username = (String) request.get("username");
		int productId = (int) request.get("productId");
		int quantity = (int) request.get("quantity");
		
		//Fetch User using username
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
		
		//update the cart item quantity
		cartService.updateCartItemQuantity(user.getUserId(), productId, quantity);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
	
	 // Delete Cart Item
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int productId = (int) request.get("productId");

        // Fetch the user using username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        // Delete the cart item
        cartService.deleteCartItem(user.getUserId(), productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

