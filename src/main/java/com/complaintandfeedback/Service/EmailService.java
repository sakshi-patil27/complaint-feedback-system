package com.complaintandfeedback.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Model.ResponseMessage;

@Service
public class EmailService {
	@Autowired
	private CommonUtils commonUtils;
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com"); // same as spring.mail.username
        message.setTo(toEmail);
        message.setSubject("Grabobite OTP Verification Code");

        String mailBody = String.format(
                "Hello 👋, \n\n" +
                "Your OTP verification code is: %s\n\n" +
                "This OTP is valid for only 5 minutes. Please do not share it with anyone.\n\n" +
                "If you did not request this, please ignore this message.\n\n" +
                "", otp);

        message.setText(mailBody);
        mailSender.send(message);
    }
   
        public void sendNotification(String to, String subject, String text) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        }

        public ResponseEntity<Object> notifyComplaintCreation(String userEmail, String assignedEmail, String complaintDetails) {
        	try {
            String userSubject = "Complaint Created Successfully – Your Issue is Being Addressed!";
            String userText = String.format(
                "Dear User,\n\n" +
                "We have successfully received your complaint. Our team is currently reviewing your issue, and we will update you as soon as possible.\n\n" +
                "Here are the details of your complaint:\n\n" +
                "%s\n\n" +
                "We appreciate your patience.\n\n" +
                "Best Regards,\n" +
                "Support Team",
                complaintDetails
            );
            sendNotification(userEmail, userSubject, userText);
            String assignedSubject = "New Complaint Assigned – Action Required";
            String assignedText = String.format(
                "Dear [Assigned User/Department],\n\n" +
                "A new complaint has been assigned to you for review and resolution. Please find the details below:\n\n" +
                "Complaint Details:\n%s\n\n" +
                "Kindly take prompt action on this complaint and keep the user informed of the status.\n\n" +
                "Best Regards,\n" +
                "Support Team",
                complaintDetails
            );
            sendNotification(assignedEmail, assignedSubject, assignedText);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseMessage("Success", "Email send successfully", null));
       	} catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        }
        }

        public ResponseEntity<Object> notifyComplaintUpdate(String userEmail, String assignedEmail, String fromStatus, String toStatus, String complaintDetails) {
        	try {
            String userSubject = "Complaint Status Updated – New Update on Your Issue";
            String userText = String.format(
                "Dear User,\n\n" +
                "We are writing to inform you that the status of your complaint has been updated. Please find the details below:\n\n" +
                "Complaint Details:\n%s\n\n" +
                "Previous Status: %s\n" +
                "Current Status: %s\n\n" +
                "We are committed to resolving your complaint as soon as possible. Thank you for your understanding.\n\n" +
                "Best Regards,\n" +
                "Support Team",
                complaintDetails, fromStatus, toStatus
            );
            sendNotification(userEmail, userSubject, userText);
            String assignedSubject = "Complaint Status Updated – Action Needed";
            String assignedText = String.format(
                "Dear [Assigned User/Department],\n\n" +
                "The status of the complaint assigned to you has been updated. Please see the details below:\n\n" +
                "Complaint Details:\n%s\n\n" +
                "Previous Status: %s\n" +
                "Current Status: %s\n\n" +
                "Please review and take necessary action as required.\n\n" +
                "Best Regards,\n" +
                "Support Team",
                complaintDetails, fromStatus, toStatus
            );
            sendNotification(assignedEmail, assignedSubject, assignedText);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseMessage("Success", "Email send successfully", null));
       	} catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        }
        }
        
        public String buildComplaintDetails(Complaint complaint) {
            if (complaint == null) {
                return "Complaint details not available.";
            }
            
            // Provide default values if any of the complaint attributes are null
            String complaintId = complaint.getComplaint_id() != null ? complaint.getComplaint_id() : "N/A";
            String subject = complaint.getSubject() != null ? complaint.getSubject() : "N/A";
            String priority = complaint.getPriority() != null ? complaint.getPriority() : "N/A";
            String description = complaint.getDescription() != null ? complaint.getDescription() : "N/A";
            
            try {
                // Ensure that the format specifiers match the arguments passed
                return String.format(
                    "Complaint ID: %s\nTitle: %s\nPriority: %s\nDescription: %s",
                    complaintId,
                    subject,
                    priority,
                    description
                );
            } catch (Exception e) {
                // Log the exception or handle it gracefully
                return "Error formatting complaint details: " + e.getMessage();
            }
        }


    }
