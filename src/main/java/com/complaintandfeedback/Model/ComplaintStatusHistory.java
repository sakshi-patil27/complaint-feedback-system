package com.complaintandfeedback.Model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	    private Timestamp changed_on;
	    private String l_changed_by;
}