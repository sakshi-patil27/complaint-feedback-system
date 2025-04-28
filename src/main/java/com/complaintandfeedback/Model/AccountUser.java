package com.complaintandfeedback.Model;


import lombok.Data;

@Data
public class AccountUser {

    private String accountId;
    private String name;
    private String email;
    private String phoneNo;
    private String password;

    private String departmentId;  // Optional for authentication
    private String roleId;
    private String orgId;
    private String oprId;

    private String createdBy;
    private String createdOn;
    private String modifiedBy;
    private String modifiedOn;
    private String isActive;
}
