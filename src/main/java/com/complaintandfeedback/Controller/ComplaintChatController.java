package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.ComplaintMessageRequest;
import com.complaintandfeedback.Service.ComplaintMessageService;

@RestController
@RequestMapping("/api/complaints/chat")
public class ComplaintChatController {

    @Autowired
    private ComplaintMessageService complaintMessageService;

    @PostMapping("/send")
    public ResponseEntity<Object> sendMessage(@RequestBody ComplaintMessageRequest req) throws Exception {
    	return complaintMessageService.sendMessageAndNotify(req);
    }

    @PostMapping("/get")
    public ResponseEntity<Object> getMessages(@RequestBody CommonRequestModel request) {
        return complaintMessageService.getMessages(request.getId());
    }
}
