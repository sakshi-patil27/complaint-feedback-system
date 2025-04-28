package com.complaintandfeedback.Model;


import lombok.Data;

@Data
public class Role {

    private String roleId;
    private Long orgId;
    private String roleName;
    private Long oprId;

    private String createdBy;
    private String createdOn;
    private String modifiedBy;
    private String modifiedOn;
    private String isActive;
}
