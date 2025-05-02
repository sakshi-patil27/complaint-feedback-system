package com.complaintandfeedback.Model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountUser {

    private String account_id;
    @NotBlank(message = "Name field cannot be empty")	
    @Size(max = 50, min = 1, message = "Name field should be between 1 to 50 characters")
    private String name;
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "PhoneNo field cannot be empty")	
    @Size(max = 10, min = 1, message = "PhoneNo field should be between 1 to 10 characters")
    private String phone_no;
    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
    private String department_id;  // Optional for authentication
    private String role_id;
    private Long org_id;
    private Long opr_id;	
    private String created_by;
    private String created_on;
    private String modified_by;
    private String modified_on;
    private String is_active;
}
