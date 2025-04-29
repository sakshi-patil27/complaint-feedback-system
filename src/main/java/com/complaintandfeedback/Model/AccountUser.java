package com.complaintandfeedback.Model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountUser {

    private String accountId;
    @NotBlank(message = "Name field cannot be empty")	
    @Size(max = 50, min = 1, message = "Name field should be between 1 to 50 characters")
    private String name;
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "PhoneNo field cannot be empty")	
    @Size(max = 10, min = 1, message = "PhoneNo field should be between 1 to 10 characters")
    private String phoneNo;
    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
    private String departmentId;  // Optional for authentication
    private String roleId;
    private Long orgId;
    private Long oprId;
    private String createdBy;
    private String createdOn;
    private String modifiedBy;
    private String modifiedOn;
    private String isActive;
}
