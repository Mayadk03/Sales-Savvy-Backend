package com.example.demo.services;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.LoginDto;
import com.example.demo.entities.JWTToken;
import com.example.demo.entities.User;
import com.example.demo.repositories.JWTTokenRepository;
import com.example.demo.repositories.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {
	private final Key SIGNING_KEY;
	private final UserRepository userrepository;
	private final JWTTokenRepository jwtTokenRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	@Autowired
	public AuthService(UserRepository userrepository, JWTTokenRepository jwtTokenRepository,@Value("${jwt.secret}") String jwtSecret) {
		super();
		this.userrepository = userrepository;
		this.jwtTokenRepository = jwtTokenRepository;
		passwordEncoder = new BCryptPasswordEncoder();
		this.SIGNING_KEY= Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
	public User authenticateUser(String username, String password) {
		User user = userrepository.findByUsername(username).orElseThrow(()-> new RuntimeException("Invalid Username"));
		if(!passwordEncoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Invalid password");
		}
		return user;
	}
	
	public String generateToken(User user) {
		String token;
		LocalDateTime currenttime = LocalDateTime.now();
		JWTToken existingToken = jwtTokenRepository.findByUserId(user.getUserId());
		if(existingToken!=null && currenttime.isBefore(existingToken.getExpireAt())) {
			token = existingToken.getToken(); 
		} else {
			token = generateNewToken(user);
			if(existingToken!=null) {
				jwtTokenRepository.delete(existingToken);
			}
			JWTToken jwtToken = new JWTToken(LocalDateTime.now().plusHours(1), token, user);
			jwtTokenRepository.save(jwtToken);
		}
		
		return token;
	}
	public String generateNewToken(User user) {
		String token = Jwts.builder()
				.setSubject(user.getUsername())
				.claim("role", user.getRole().name())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis()+3600000))
				.signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
				.compact();
		return token;
	}
	public boolean validateToken(String token) {
		try {
			System.err.println("Validating token");
			
			//parse and validate token
			Jwts.parserBuilder()
			.setSigningKey(SIGNING_KEY)
			.build()
			.parse(token);
			
			Optional<JWTToken> jwtToken = Optional.of(jwtTokenRepository.findByToken(token));
			if(jwtToken.isPresent()) {
				System.err.println("Token expiry: "+jwtToken.get().getExpireAt());
				System.err.println("Current time : "+LocalDateTime.now());
				return jwtToken.get().getExpireAt().isAfter(LocalDateTime.now());
			}
			
			return false;
		} catch (Exception e) {
			System.err.println("Token validation failed: "+e.getMessage());
			return false;
		}
	}
	public String extractUsername(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(SIGNING_KEY)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
	public void logOut(User user) {
		jwtTokenRepository.deleteByUserId(user.getUserId());
	}
}
