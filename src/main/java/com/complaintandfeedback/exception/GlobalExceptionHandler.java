package com.complaintandfeedback.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.complaintandfeedback.Service.CommonUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private CommonUtils commonUtils;

    // Handle expired JWT token exception
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex) {
        // Use the responseErrorHeader method from CommonUtils
        return commonUtils.responseErrorHeader(ex, "JWT Token Expired", HttpStatus.UNAUTHORIZED, "Token has expired");
    }

    // Handle invalid JWT token exception
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleInvalidJwtException(JwtException ex) {
        // Use the responseErrorHeader method from CommonUtils
        return commonUtils.responseErrorHeader(ex, "Invalid JWT Token", HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    // Handle authentication exception (e.g., failed login or invalid credentials)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        // Use the responseErrorHeader method from CommonUtils
        return commonUtils.responseErrorHeader(ex, "Authentication", HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Handle access denied (authorization) exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        // Use the responseErrorHeader method from CommonUtils
        return commonUtils.responseErrorHeader(ex, "Authorization", HttpStatus.FORBIDDEN, "Access Denied");
    }

    // Handle insufficient authentication (e.g., missing or invalid token)
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Object> handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
        // Use the responseErrorHeader method from CommonUtils
        return commonUtils.responseErrorHeader(ex, "Insufficient Authentication", HttpStatus.UNAUTHORIZED, "Authentication is required");
    }

    // Handle all generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        // Use the responseErrorHeader method from CommonUtils
        return commonUtils.responseErrorHeader(ex, "General Error", HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
