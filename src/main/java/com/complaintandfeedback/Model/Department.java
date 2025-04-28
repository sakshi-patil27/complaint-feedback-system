package com.complaintandfeedback.Model;


import lombok.Data;

import java.sql.Timestamp;

@Data
public class Department {
    private String departmentId;
    private Long orgId;
    private String departmentName;
    private String description;
    private String createdBy;
    private Timestamp createdOn;
    private String modifiedBy;
    private Timestamp modifiedOn;
    private String isActive;
    private Long oprId;
}
