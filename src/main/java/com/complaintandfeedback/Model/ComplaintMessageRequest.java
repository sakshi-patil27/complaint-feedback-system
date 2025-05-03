package com.complaintandfeedback.Model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintMessageRequest {
	private String complaintId;
	private String senderId;
	private String receiverId;
	private String message;
}
