package com.complaintandfeedback.Model;


import lombok.Data;

@Data
public class Role {

    private String roleId;
    private String orgId;
    private String roleName;
    private String oprId;

    private String createdBy;
    private String createdOn;
    private String modifiedBy;
    private String modifiedOn;
    private String isActive;
}
