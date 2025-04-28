package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.complaintandfeedback.DTO.EmailRequest;
import com.complaintandfeedback.DTO.LoginRequest;
import com.complaintandfeedback.DTO.OtpRequest;
import com.complaintandfeedback.DTO.UpdateUserRequest;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Service.AuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody AccountUser accountUser) {
			return authenticationService.registerUser(accountUser);
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
		try {
			return authenticationService.login(loginRequest.getEmail(), loginRequest.getPassword());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}

	}

	@PutMapping("/update")
	public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest updateUserRequest) {
		try {
			return authenticationService.updateUser(updateUserRequest);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}
	}
	
	@PostMapping("/send-otp")
	public ResponseEntity<String> sendOtp(@RequestBody EmailRequest request) {
		try {

		    String otp = authenticationService.generateOtp(request.getEmail());
		    return ResponseEntity.ok("OTP sent to your email.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}

	}

	@PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest request) {
	    boolean isValid = authenticationService.verifyOtp(request.getEmail(), request.getOtp());
	    if (isValid) {
	        return ResponseEntity.ok("OTP Verified Successfully!");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or Expired OTP.");
	    }
	}
	
	// Forget Password API (Reset Password)
    @PostMapping("/forget-password")
    public ResponseEntity<?> resetPassword(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        String newPassword = request.getPassword();
        boolean isPasswordReset = authenticationService.resetPassword(email, newPassword);
        if (isPasswordReset) {
            return new ResponseEntity<>("Password reset successful.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to reset password.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
