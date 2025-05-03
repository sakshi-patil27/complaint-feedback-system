package com.complaintandfeedback.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationResponse {
    private boolean success;
    private String message;
    private String token;
    private String userId;
    private String role;
    private String username;
      private Long org_id;    
      private Long opr_id;
      private String l_org_name;
      private String l_role_name;
      private String l_department_Id;
      private String l_department_name;
}

