package com.complaintandfeedback.Model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatusHistory {
	    private String complaint_status_history_id;
	    private String complaint_id;
	    private String from_status;
	    private String to_status;
	    private String reason;
	    private String changed_by;
	    private LocalDateTime changed_on = LocalDateTime.now();
	}