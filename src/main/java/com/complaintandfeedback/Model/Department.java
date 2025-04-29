package com.complaintandfeedback.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {
    private String department_id;
    private Long org_id;
    @NotBlank(message = "Department name must not be empty")
    @Size(min = 5, max = 20, message = "Department name must be between 5 and 20 characters")
    private String department_name;
    @NotBlank(message = "Description must not be empty")
    @Size(min = 5, max = 100, message = "Description must be between 5 and 100 characters")
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