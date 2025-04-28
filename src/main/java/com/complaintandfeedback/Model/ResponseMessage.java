package com.complaintandfeedback.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMessage {

    private String status;
    private String statusMsg;
    private String Id;

   
}
