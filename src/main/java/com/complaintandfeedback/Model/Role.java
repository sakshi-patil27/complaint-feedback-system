package com.complaintandfeedback.Model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Role {

    private String role_id;
    private Long org_id;
    @NotBlank(message = "Role name must not be empty")
	@Size(min = 5, max = 100, message = "Role name must be between 5 and 100 characters")
    private String role_name;
    private Long opr_id;
    private String created_by;
    private String created_on;
    private String modified_by;
    private String modified_on;
    private String is_active;
}