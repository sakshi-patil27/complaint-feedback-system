package com.complaintandfeedback.Model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Suggestion {
	
	    private String suggestion_id;  
	    private Long org_id;  
	    @NotBlank(message = "Subject is required")
	    @Size(max = 30, message = "Subject must be at most 30 characters")
	    private String subject;     
	    @NotBlank(message = "Description is required")
	    @Size(max = 500, message = "Description can be up to 500 characters")
	    private String description;  
	    private String department_id; 
	    private String created_by;  
	    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	    private Timestamp created_on;
	    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	    private Timestamp modified_on; 
	    private String modified_by; 
	    private String attachment_id;  
	    private Long opr_id;
	
}
