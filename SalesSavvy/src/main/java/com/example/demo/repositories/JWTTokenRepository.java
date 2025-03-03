package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.JWTToken;

import jakarta.transaction.Transactional;
public interface JWTTokenRepository extends JpaRepository<JWTToken, Integer> {
	@Query("SELECT t FROM JWTToken t WHERE t.user.userId = :userId")
	JWTToken findByUserId(@Param("userId") int userId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM JWTToken t WHERE t.user.userId = :userId")
	void deleteByUserId(@Param("userId") int userId);
	
	JWTToken findByToken(String token);
	
}
