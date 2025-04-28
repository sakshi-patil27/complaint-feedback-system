package com.complaintandfeedback.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String phoneNo;
    private String password;
    private String departmentId;
    private String roleId;
    private String orgId;
    private String oprId;
    private String createdBy;
}

