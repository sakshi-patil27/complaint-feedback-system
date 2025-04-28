package com.complaintandfeedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonRequestModel {
	  private String orgId;
	  private String oprId;
	  private String email;
}
