package com.complaintandfeedback.DTO;

import lombok.Data;

@Data
public class AuthenticationUserDTO {

    private String email;
    private String password;
    private String roleName;   // Example: ADMIN, CUSTOMER, etc.
}
