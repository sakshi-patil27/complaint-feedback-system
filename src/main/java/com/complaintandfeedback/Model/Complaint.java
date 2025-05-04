package com.complaintandfeedback.Model;


import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Complaint {
	private String complaint_id;

    private Long org_id;

    private Long opr_id;
    
    @NotBlank(message = "Subject field cannot be empty")	
    @Size(max = 100, min = 1, message = "Subject field should be between 1 to 100 characters")
    private String subject;
    
    @NotBlank(message = "Subject field cannot be empty")	
    @Size(max = 200, min = 1, message = "Subject field should be between 1 to 200 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    private String priority;

    private String status;

    private String department_id;

    private String created_by;

    private String assigned_to;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp created_on;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp modified_on;

    private String modified_by;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp due_date;

    private String is_active;
    
    private String l_previous_status;
    
    private String l_department_name;
    
    private String l_created_by;
    
    private String l_assigned_to;
    
}
