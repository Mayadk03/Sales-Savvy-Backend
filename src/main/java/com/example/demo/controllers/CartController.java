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
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5175", allowCredentials = "true")
public class CartController {

    private final UserRepository userRepository;
    private final CartService cartService;

    public CartController(UserRepository userRepository, CartService cartService) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // Get cart count
    @GetMapping("/items/count")
    public ResponseEntity<Integer> getCartCount(@RequestParam String username) {
        Optional<User> opuser = userRepository.findByUsername(username);
        if (opuser.isEmpty()) {
            return ResponseEntity.ok(0);
        }
        int count = cartService.getCartItemCount(opuser.get().getUserId());
        return ResponseEntity.ok(count);
    }

    // Get all cart items
    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItem(HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> response = cartService.getCartItems(user.getUserId());
        return ResponseEntity.ok(response);
    }

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Received addToCart request: " + request);
            
            String username = (String) request.get("username");
            Integer productId = (Integer) request.get("productId");
            Integer quantity = (Integer) request.getOrDefault("quantity", 1);

            if (username == null || productId == null) {
                System.out.println("Invalid request: missing username or productId");
                return ResponseEntity.badRequest().body("Username and productId are required.");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            System.out.println("User found: " + user.getUsername());

            cartService.addToCart(user.getUserId(), productId, quantity);
            System.out.println("Product added to cart: ProductID=" + productId + ", Quantity=" + quantity);
            
            return ResponseEntity.status(HttpStatus.CREATED).body("Product added to cart.");

        } catch (Exception e) {
            System.out.println("Error adding to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Update cart item quantity
    @PutMapping("/update")
    public ResponseEntity<String> updateCartItemQuantity(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Update started");
            
            String username = (String) request.get("username");
            Integer productId = (Integer) request.get("productId");
            Integer quantity = (Integer) request.get("quantity");

            if (username == null || productId == null || quantity == null) {
                return ResponseEntity.badRequest().body("Username, productId, and quantity are required.");
            }

            System.out.println("User: " + username + ", Product ID: " + productId + ", Quantity: " + quantity);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            cartService.updateCartItemQuantity(user.getUserId(), productId, quantity);
            return ResponseEntity.ok("Cart item updated successfully.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Delete cart item
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCartItem(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            Integer productId = (Integer) request.get("productId");

            if (username == null || productId == null) {
                return ResponseEntity.badRequest().body("Username and ProductId are required.");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            cartService.deleteCartItem(user.getUserId(), productId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
