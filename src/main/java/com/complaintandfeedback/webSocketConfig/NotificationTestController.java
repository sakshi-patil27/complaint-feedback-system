package com.complaintandfeedback.webSocketConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/test")
public class NotificationTestController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/notify/{id}")
    public ResponseEntity<?> send(@PathVariable String id) {
        notificationService.sendNotification(id, "Hello from backend!");
        return ResponseEntity.ok("Sent");
    }
}
