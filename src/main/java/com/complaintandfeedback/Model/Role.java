package com.complaintandfeedback.Model;


import lombok.Data;

@Data
public class Role {

    private String role_id;
    private Long org_id;
    private String role_name;
    private Long opr_id;

    private String created_by;
    private String created_on;
    private String modified_by;
    private String modified_on;
    private String is_active;
}