package com.complaintandfeedback.Model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Size(max = 16)
    private String feedback_id;
    @NotBlank(message = "Subject is required")
    @Size(max = 30, message = "Subject must be at most 30 characters")
    private String subject;
    @Size(max = 500, message = "Description can be up to 500 characters")
    private String description;
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;
    private String complaint_id;
    private String created_by;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp created_on;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp modified_on;
    private String modified_by;
    private Long org_id;
    private Long opr_id;
    private String is_active;
}
