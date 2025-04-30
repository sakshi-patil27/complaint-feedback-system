package com.complaintandfeedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonRequestModel {
	  private Long orgId;
	  private Long oprId;
	  private String email;
	  private String password;
	  private String otp;
	  private String Id;
	  private String entity_type;
}
