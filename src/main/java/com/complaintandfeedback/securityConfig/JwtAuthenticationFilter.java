package com.complaintandfeedback.securityConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private UserDetailsService userDetailsService;

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
			System.out.println("Token invalid or missing.");
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
