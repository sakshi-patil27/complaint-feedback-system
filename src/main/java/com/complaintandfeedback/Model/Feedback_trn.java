package com.complaintandfeedback.Model;

import java.sql.Timestamp;

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
public class Feedback_trn {
    @Size(max = 16)
    private String feedbacks_id;
    @NotBlank(message = "Subject is required")
    @Size(max = 30, message = "Subject must be at most 30 characters")
    private String subject;
    @Size(max = 500, message = "Description can be up to 500 characters")
    private String description;
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;
    private Complaint linked_complaint_id;
    private String created_by;
    private Timestamp created_on;
    private Timestamp modified_on;
    private String modified_by;
}
