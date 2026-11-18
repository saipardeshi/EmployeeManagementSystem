package com.mainapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mainapp.entity.User;
import com.mainapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// Allow CORS preflight requests
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}

		String path = request.getRequestURI();
		String method = request.getMethod().toUpperCase();

		// Identify caller via JWT Bearer token or HttpSession
		String token = extractBearerToken(request);
		String username = null;
		String role = null;

		if (token != null && jwtUtil.validateToken(token)) {
			username = jwtUtil.getUsername(token);
			role = jwtUtil.getRole(token);
		} else {
			HttpSession session = request.getSession(false);
			if (session != null && session.getAttribute("username") != null) {
				username = (String) session.getAttribute("username");
				role = (String) session.getAttribute("role");
				if (role == null && username != null) {
					role = userService.getUser(username).map(User::getRole).orElse("ROLE_USER");
					session.setAttribute("role", role);
				}
			}
		}

		// Store authenticated user context in request
		if (username != null) {
			request.setAttribute("authenticatedUser", username);
			request.setAttribute("authenticatedRole", role != null ? role : "ROLE_USER");
		}

		// Protect Admin mutation endpoints: POST, PUT, DELETE under /api/employees/**
		if (path.startsWith("/api/employees")) {
			boolean isMutation = "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);

			if (isMutation) {
				// 1. Check if authenticated
				if (username == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please sign in as an Admin.\"}");
					return false;
				}

				// 2. Check if Admin
				if (!"ROLE_ADMIN".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied. Admin privileges required for this feature.\"}");
					return false;
				}
			}
		}

		return true;
	}

	private String extractBearerToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7).trim();
		}
		return null;
	}
}
