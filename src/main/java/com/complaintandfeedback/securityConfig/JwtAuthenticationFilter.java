package com.complaintandfeedback.securityConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.complaintandfeedback.Service.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private CommonUtils commonUtils;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String path = request.getRequestURI();

		// Skip authentication for these public endpoints
		if (path.equals("/api/auth/register") || path.equals("/api/auth/login") || path.equals("api/auth/send-otp")
				|| path.equals("api/auth/verify-otp")) {
			chain.doFilter(request, response);
			return;
		}
		// Your existing logic below
		String token = getTokenFromRequest(request);
		if (token != null && jwtTokenProvider.validateToken(token)) {
			String email = jwtTokenProvider.getUsernameFromToken(token);
			List<String> roles = jwtTokenProvider.getRolesFromToken(token);

			Collection<? extends GrantedAuthority> authorities = roles.stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null,
					authorities);

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);

		} else {
			    // Token is invalid, returning an error response
			    Exception exception = new Exception("Invalid or missing token");
			    ResponseEntity<Object> responseEntity = commonUtils.responseErrorHeader(
			        exception, 
			        "JWT", 
			        HttpStatus.UNAUTHORIZED, 
			        "Invalid or missing token"
			    );

			    // Set response properties
			    response.setStatus(responseEntity.getStatusCodeValue());
			    response.setContentType("application/json");

			    // Convert Java object to JSON using ObjectMapper
			    ObjectMapper mapper = new ObjectMapper();
			    String jsonResponse = mapper.writeValueAsString(responseEntity.getBody());

			    response.getWriter().write(jsonResponse);
			    return; // Exit after returning the respon

		}

		chain.doFilter(request, response);
	}

	private String getTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}
