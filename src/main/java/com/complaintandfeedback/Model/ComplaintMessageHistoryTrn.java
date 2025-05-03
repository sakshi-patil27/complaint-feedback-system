package com.complaintandfeedback.Model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintMessageHistoryTrn {
	    private String complaint_message_history_id;
	    private String complaint_id;
	    private String sender_id;
	    private String receiver_id;
	    private String message;
	    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	    private Timestamp sent_on;
	}