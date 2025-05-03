package com.complaintandfeedback.Model;

import java.sql.Timestamp;

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
	    private Timestamp sent_on;
	}