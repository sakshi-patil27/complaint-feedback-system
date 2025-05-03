package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Service.AuthenticationService;

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
	public ResponseEntity<?> loginUser(@RequestBody CommonRequestModel loginRequest) {
		try {
			return authenticationService.login(loginRequest.getEmail(), loginRequest.getPassword());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}

	}

	@PutMapping("/update")
	public ResponseEntity<?> updateUser(@RequestBody AccountUser updateUserRequest) {
		try {
			return authenticationService.updateUser(updateUserRequest);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}
	}
	
	@PostMapping("/send-otp")
	public ResponseEntity<String> sendOtp(@RequestBody CommonRequestModel request) {
		try {

		    String otp = authenticationService.generateOtp(request.getEmail());
		    return ResponseEntity.ok("OTP sent to your email.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		}

	}

	@PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(@RequestBody CommonRequestModel request) {
	    boolean isValid = authenticationService.verifyOtp(request.getEmail(), request.getOtp());
	    if (isValid) {
	        return ResponseEntity.ok("OTP Verified Successfully!");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or Expired OTP.");
	    }
	}
	
	// Forget Password API (Reset Password)
    @PostMapping("/forget-password")
    public ResponseEntity<?> resetPassword(@RequestBody CommonRequestModel request) {
        String email = request.getEmail();
        String newPassword = request.getPassword();
        boolean isPasswordReset = authenticationService.resetPassword(email, newPassword);
        if (isPasswordReset) {
            return new ResponseEntity<>("Password reset successful.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to reset password.", HttpStatus.INTERNAL_SERVER_ERROR);
        }              
    }
    
   // get list of users according to the department 
   @PostMapping("/getUserByDepartment")
   public ResponseEntity<Object> getUserByDepartment(@RequestBody CommonRequestModel commonRequestModel){
	   return authenticationService.getUserByDepartment(commonRequestModel);
   }
//get user by id
   @PostMapping("/{accountId}")
   public ResponseEntity<Object> getUserByAccountId(@RequestBody CommonRequestModel commonRequestModel) {
       return authenticationService.getUserByAccountId(commonRequestModel.getId());
   }
    
}
