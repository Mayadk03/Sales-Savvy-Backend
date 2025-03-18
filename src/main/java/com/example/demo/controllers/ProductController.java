package com.example.demo.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5175", allowCredentials = "true")
@RequestMapping("/api")
public class ProductController {
	@Autowired
	private ProductService productService;

	@GetMapping("/products")
	public ResponseEntity<Map<String, Object>> getProducts(@RequestParam(required = false) String category, HttpServletRequest request) {
		try {
			User authenticatedUser = (User) request.getAttribute("authenticatedUser");
			System.out.println("Authenticated User: " + authenticatedUser);

			if(authenticatedUser==null) {
				return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
			}
			//Fetch products based on the category filter
			List<Product> products = productService.getProductsByCategory(category);

			//Build the response
			Map<String, Object> response = new HashMap<>();

			//Map for user
			Map<String, String> userinfo = new HashMap<>();
			userinfo.put("name", authenticatedUser.getUsername());
			userinfo.put("role", authenticatedUser.getRole().name());

			response.put("user", userinfo);

			//Add porduct details
			List<Map<String, Object>> productList = new ArrayList<>();

			for(Product product: products) {
				Map<String, Object> productDetails = new HashMap<>();
				productDetails.put("Product_id", product.getProductId());
				productDetails.put("name", product.getName());
				productDetails.put("description", product.getDescription());
				productDetails.put("price", product.getPrice());
				productDetails.put("stock", product.getStock());

				List<String> images = productService.getProductImages(product.getProductId());
				productDetails.put("images", images);

				productList.add(productDetails);
			}

			response.put("products", productList);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
