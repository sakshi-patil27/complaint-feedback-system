package com.complaintandfeedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    private boolean success;
    private String message;
    private String token;
    private String userId;
    private String role;
    private String username;
	private Long org_id;	
	private Long opr_id;
	public AuthenticationResponse(boolean success, String message, String token, String userId, String role,
			String username, Long org_id, Long opr_id) {
		super();
		this.success = success;
		this.message = message;
		this.token = token;
		this.userId = userId;
		this.role = role;
		this.username = username;
		this.org_id = org_id;
		this.opr_id = opr_id;
	}	
}

