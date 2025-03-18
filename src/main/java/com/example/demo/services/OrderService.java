package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.entities.User;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;

@Service
public class OrderService {
	
	private OrderRepository orderRepository;
	
	private OrderItemRepository orderItemRepository;
	
	private ProductRepository productRepository;
	
	private ProductImageRepository productImageRepository;

	public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			ProductRepository productRepository,ProductImageRepository productImageRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
	}
	
	public Map<String, Object> getOrderForUser(User user) {
		List<OrderItem> orderItems =  orderItemRepository.findSuccessfulOrderItemsByUserId(user.getUserId());
		Map<String, Object> response = new HashMap<>();
		response.put("username", user.getUsername());
		response.put("role", user.getRole());
		
		List<Map<String, Object>> products = new ArrayList<>();
		for(OrderItem item: orderItems) {
			Product product =  productRepository.findById(item.getProductId()).orElse(null);
			if(product == null) {
				continue;
			}
			List<ProductImage> images = productImageRepository.findByProduct_ProductId(product.getProductId());
			String imageUrl =  images.isEmpty()?null:images.get(0).getImageUrl();
			Map<String, Object> productDetails = new HashMap<>();
			productDetails.put("order_id", item.getOrder().getOrderId());
			productDetails.put("quantity", item.getQuantity());
			productDetails.put("total_price", item.getTotalPrice());
			productDetails.put("image_url", imageUrl);
			productDetails.put("product_id", product.getProductId());
			productDetails.put("description", product.getDescription());
			productDetails.put("price_per_unit", item.getPricePerUnit());
			
			products.add(productDetails);
		}
		response.put("products", products);
		
		return response;
	}
	
	
}
