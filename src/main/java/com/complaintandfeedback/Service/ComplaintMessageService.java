package com.complaintandfeedback.Service;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Model.ComplaintMessageHistoryTrn;
import com.complaintandfeedback.Model.ComplaintMessageRequest;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class ComplaintMessageService {

	@Autowired
	private CommonUtils commonUtils;
    @Autowired
    private ComplaintMessageHistoryService complaintMessageHistoryService;

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<Object> sendMessageAndNotify(ComplaintMessageRequest req) throws Exception{
        ResponseEntity<Object> receiverResponse = authenticationService.getUserByAccountId(req.getReceiverId());
        if (!receiverResponse.getStatusCode().equals(HttpStatus.OK) || receiverResponse.getBody() == null) {
            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
                    "Failed to retrieve receiver details");
        }

        AccountUser receiver = objectMapper.convertValue(receiverResponse.getBody(), AccountUser.class);
        if (receiver.getEmail() == null || receiver.getEmail().isBlank()) {
            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
                    "Receiver email is missing");
        }

        ResponseEntity<Object> senderResponse = authenticationService.getUserByAccountId(req.getSenderId());
        if (!senderResponse.getStatusCode().equals(HttpStatus.OK) || senderResponse.getBody() == null) {
            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
                    "Failed to retrieve sender details");
        }

        AccountUser sender = objectMapper.convertValue(senderResponse.getBody(), AccountUser.class);
        if (sender.getEmail() == null || sender.getEmail().isBlank()) {
            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
                    "Sender email is missing");
        }

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true);
        helper.setTo(receiver.getEmail());
        helper.setFrom(new InternetAddress("noreply@yourdomain.com", sender.getEmail())); // use your actual configured SMTP sender
        helper.setReplyTo(sender.getEmail());      // replies go to actual sender
        helper.setSubject("New message regarding complaint: " + req.getComplaintId());
        helper.setText("Message: " + req.getMessage(), false);
        mailSender.send(mime);

        ComplaintMessageHistoryTrn msg = new ComplaintMessageHistoryTrn();
        msg.setComplaint_message_history_id(UUID.randomUUID().toString().substring(0, 16));
        msg.setComplaint_id(req.getComplaintId());
        msg.setSender_id(req.getSenderId());
        msg.setReceiver_id(req.getReceiverId());
        msg.setMessage(req.getMessage());
        msg.setSent_on(Timestamp.valueOf(LocalDateTime.now()));
        complaintMessageHistoryService.saveComplaintMessage(msg);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseMessage("Success", "Message sent and saved successfully", null));
    }


    public ResponseEntity<Object> getMessages(String complaintId) {
        return complaintMessageHistoryService.getMessagesByComplaintId(complaintId);
    }
}

