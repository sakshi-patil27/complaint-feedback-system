package com.complaintandfeedback.DTO;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String accountId;    // We need ID to identify the user
    private String name;
    private String phoneNo;
    private String departmentId;
    private String roleId;
    private String modifiedBy;
}
