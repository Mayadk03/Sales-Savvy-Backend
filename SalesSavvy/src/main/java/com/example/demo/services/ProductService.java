package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;

@Service
public class ProductService {
	private ProductRepository productRepository;
	private ProductImageRepository imageRepository;
	private CategoryRepository categoryRepository;
	@Autowired
	public ProductService(ProductRepository productRepository, ProductImageRepository imageRepository,
			CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.categoryRepository = categoryRepository;
	}
	
	public List<Product> getProductsByCategory(String categoryName) {
		if(categoryName!=null && !categoryName.isEmpty()) {
			Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);
			if(categoryOpt.isPresent()) {
				Category category = categoryOpt.get();
				return productRepository.findByCategory_CategoryId(category.getCategoryId());
			}  else {
				throw new RuntimeException("category Not found");
			}
		} else {
			return productRepository.findAll();
		}
	}
	
	public List<String> getProductImages(Integer productId) {
		List<ProductImage> productImages = imageRepository.findByProduct_ProductId(productId);
		List<String> imageUrls = new ArrayList<>();
		for(ProductImage image : productImages) {
			imageUrls.add(image.getImageUrl());
		}
		return imageUrls;
				
	}
	
}
