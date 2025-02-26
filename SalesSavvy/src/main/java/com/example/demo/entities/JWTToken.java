package com.example.demo.entities;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jwt_tokens")
public class JWTToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
private int token_id;
	@Column
private LocalDateTime createdAt;
	@Column
private LocalDateTime expiresAt;
	@Column
private String token;
@ManyToOne
@JoinColumn(name="user_id")
User user;
public JWTToken() {
	super();
	// TODO Auto-generated constructor stub
}
public JWTToken(int token_id, LocalDateTime createdAt, LocalDateTime expiresAt, String token, User user) {
	super();
	this.token_id = token_id;
	this.createdAt = createdAt;
	this.expiresAt = expiresAt;
	this.token = token;
	this.user = user;
}
public JWTToken(LocalDateTime expiresAt, String token, User user) {
	super();
	this.expiresAt = expiresAt;
	this.token = token;
	this.user = user;
}
public int getToken_id() {
	return token_id;
}
public void setToken_id(int token_id) {
	this.token_id = token_id;
}
public LocalDateTime getCreatedAt() {
	return createdAt;
}
public void setCreatedAt(LocalDateTime createdAt) {
	this.createdAt = createdAt;
}
public LocalDateTime getExpireAt() {
	return expiresAt;
}
public void setExpireAt(LocalDateTime expiresAt) {
	this.expiresAt = expiresAt;
}
public String getToken() {
	return token;
}
public void setToken(String token) {
	this.token = token;
}
public User getUser() {
	return user;
}
public void setUser(User user) {
	this.user = user;
}


}
