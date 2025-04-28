package com.complaintandfeedback.exception;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.complaintandfeedback.Model.ResponseHeaderModel;
import com.complaintandfeedback.Service.CommonUtils;

@ControllerAdvice
public class GlobalExceptionHandler {

	@Autowired
	private CommonUtils commonUtils;
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> methodArgumentNotValidExceptionhandler(MethodArgumentNotValidException ex){
		
		String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(",\n")); 
		return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
				message);
	}
	
}
