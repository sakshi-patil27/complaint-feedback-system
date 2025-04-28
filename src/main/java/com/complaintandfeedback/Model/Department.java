package com.complaintandfeedback.Model;


import lombok.Data;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class Department {
    private String department_id;
    private Long org_id;
    private String department_name;
    private String description;
    private String created_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp created_on;
    private String modified_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp modified_on;
    private String is_active;
    private Long opr_id;
}