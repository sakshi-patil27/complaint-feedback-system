package com.complaintandfeedback.webSocketConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String recipientId, String messageText) {
        NotificationMessage message = new NotificationMessage();
        message.setRecipientId(recipientId);
        message.setMessage(messageText);
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, message);
    }
}
