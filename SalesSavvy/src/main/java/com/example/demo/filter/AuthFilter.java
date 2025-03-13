package com.example.demo.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.AuthService;
import com.example.demo.services.UserService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
@Component
public class AuthFilter implements Filter{

	private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
	private final UserRepository repository;
	private final AuthService authService;
		
	public AuthFilter(UserRepository repository,UserService userService,AuthService authService) {
		System.out.println("Filter started");
		this.repository=repository;
		this.authService = authService;
	}
	
	private static final String ALLOWED_ORIGIN = "http://localhost:5173";
	private static final String[] UNAUTHENTICATED_PATHS = {"/api/user/register","/api/auth/login"};
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
			
		try {
			executeFilterLogic(request, response, chain);
			
		} catch (Exception e) {
			logger.error("Unexpected error in authenticationFilter");
			sendErrorResponse((HttpServletResponse)response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
		
	}
	
	
	public void sendErrorResponse(HttpServletResponse response,int statusCode,String msg) throws IOException {
		response.setStatus(statusCode);
		response.getWriter().write(msg);
		
	}
	
	public void executeFilterLogic(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String url = httpRequest.getRequestURI();
		logger.info("Request URI: {}",url);
		
		if(Arrays.asList(UNAUTHENTICATED_PATHS).contains(url)) {
			chain.doFilter(request, response);
			return;
		}
		
		// Handle preflight (Option) request
		if(httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
			setCORSHeaders(httpResponse);
		}
	
	String token = getAuthTokenFromCookies(httpRequest);
	System.out.println(token);
	if(token == null || !authService.validateToken(token)) {
		sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User invalid or missing");
		return;
	}
	
	//Extract username and verify user
	String username = authService.extractUsername(token);
	Optional<User> userOptional = repository.findByUsername(username);
	if(userOptional.isEmpty()) {
		sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User not found");
		return;
	}
	
	User authenticatorUser  = userOptional.get();
	Role role = authenticatorUser.getRole();
	logger.info("Authenticated user : {}, Role: {}",authenticatorUser.getUsername(),role);
	
	//role based access
	if (url.startsWith("/admin/") && role != Role.ADMIN) {
		sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbiddden: Admin access required");
		return;
	}
	if(url.startsWith("/api/")&& (role!= Role.CUSTOMER && role != Role.ADMIN)) {
		System.err.println("Checking .........."+url+"   "+role);
		System.err.println("HERE LOGIC WORKED");
		sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
		return;
	}
	
	// Attach user details to request
	httpRequest.setAttribute("authenticatedUser", authenticatorUser);
	chain.doFilter(request, response);
	
	}
	private String getAuthTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			return Arrays.stream(cookies).filter(cookie -> "authToken".equals(cookie.getName()))
					.map(Cookie::getValue)
					.findFirst()
					.orElse(null);
		}
		return null;
	}
	private void setCORSHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
		response.setHeader("Access-Control-Allow-Methods", "GET, POST,PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
