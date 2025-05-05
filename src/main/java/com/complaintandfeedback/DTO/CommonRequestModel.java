package com.complaintandfeedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonRequestModel {
	  private Long org_id;
	  private Long opr_id;
	  private String email;
	  private String password;
	  private String otp;
	  private String id;
	  private String entity_type;
}
