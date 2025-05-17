package com.complaintandfeedback.webSocketConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String message;
    private String recipientId;
}

