package com.complaintandfeedback.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Model.Feedback;
import com.complaintandfeedback.Model.ResponseMessage;
import com.complaintandfeedback.Model.Suggestion;

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

		String mailBody = String.format("Hello 👋, \n\n" + "Your OTP verification code is: %s\n\n"
				+ "This OTP is valid for only 5 minutes. Please do not share it with anyone.\n\n"
				+ "If you did not request this, please ignore this message.\n\n" + "", otp);

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

	public ResponseEntity<Object> notifyComplaintCreation(String userEmail, String assignedEmail,
			String complaintDetails) {
		try {
			String userSubject = "Complaint Created Successfully – Your Issue is Being Addressed!";
			String userText = String.format("Dear User,\n\n"
					+ "We have successfully received your complaint. Our team is currently reviewing your issue, and we will update you as soon as possible.\n\n"
					+ "Here are the details of your complaint:\n\n" + "%s\n\n" + "We appreciate your patience.\n\n"
					+ "Best Regards,\n" + "Support Team", complaintDetails);
			sendNotification(userEmail, userSubject, userText);
			String assignedSubject = "New Complaint Assigned – Action Required";
			String assignedText = String.format("Dear [Assigned User/Department],\n\n"
					+ "A new complaint has been assigned to you for review and resolution. Please find the details below:\n\n"
					+ "Complaint Details:\n%s\n\n"
					+ "Kindly take prompt action on this complaint and keep the user informed of the status.\n\n"
					+ "Best Regards,\n" + "Support Team", complaintDetails);
			sendNotification(assignedEmail, assignedSubject, assignedText);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseMessage("Success", "Email send successfully", null));
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
		}
	}

	public ResponseEntity<Object> notifyComplaintUpdate(String userEmail, String assignedEmail, String fromStatus,
			String toStatus, String complaintDetails) {
		try {
			String userSubject = "Complaint Status Updated – New Update on Your Issue";
			String userText = String.format("Dear User,\n\n"
					+ "We are writing to inform you that the status of your complaint has been updated. Please find the details below:\n\n"
					+ "Complaint Details:\n%s\n\n" + "Previous Status: %s\n" + "Current Status: %s\n\n"
					+ "We are committed to resolving your complaint as soon as possible. Thank you for your understanding.\n\n"
					+ "Best Regards,\n" + "Support Team", complaintDetails, fromStatus, toStatus);
			sendNotification(userEmail, userSubject, userText);
			String assignedSubject = "Complaint Status Updated – Action Needed";
			String assignedText = String.format("Dear [Assigned User/Department],\n\n"
					+ "The status of the complaint assigned to you has been updated. Please see the details below:\n\n"
					+ "Complaint Details:\n%s\n\n" + "Previous Status: %s\n" + "Current Status: %s\n\n"
					+ "Please review and take necessary action as required.\n\n" + "Best Regards,\n" + "Support Team",
					complaintDetails, fromStatus, toStatus);
			sendNotification(assignedEmail, assignedSubject, assignedText);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseMessage("Success", "Email send successfully", null));
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
		}
	}
	
	public ResponseEntity<Object> notifySuggestionCreation(String userEmail, String HODEmail,
			String suggestionDetails){
		
		try {

			String userSubject = "Suggestion is created succesfully - Your Suggestions are Highly Appreciated";			
			String userText = String.format("Dear User,\n\n"
                    + "Thank you for your valuable suggestion. We appreciate your input and will consider it carefully as we work towards improving our services.\n\n"
                    + "Here are the details of your suggestion:\n\n" + "%s\n\n" 
                    + "We appreciate your contribution and look forward to hearing more from you.\n\n"
                    + "Best Regards,\n" + "Support Team", suggestionDetails);
			sendNotification(userEmail, userSubject, userText);
			
			String assignedSubject = "New Suggestion Assigned – Action Required";
			String assignedText = String.format("Dear HOD,\n\n"
			                    + "A new suggestion has been assigned to you for review and consideration. Please find the details below:\n\n"
			                    + "Suggestion Details:\n%s\n\n"
			                    + "Kindly review the suggestion and take appropriate action as required. Please keep the user informed of the status.\n\n"
			                    + "Best Regards,\n" + "Support Team", suggestionDetails);
			sendNotification(HODEmail, assignedSubject, assignedText);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseMessage("Success", "Email send successfully", null));
		}
		catch(Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
		}
		
	}
	
	public ResponseEntity<Object> notifyFeedbackCreation(String userEmail, String HODEmail, String assignedEmail,
			String feedbackDetails){
		
		try {
			// Email to user
			String userSubject = "Feedback is received – Thank you for your input";			
			String userText = String.format("Dear User,\n\n"
                    + "Thank you for your valuable feedback. We appreciate your input and will consider it carefully as we work towards improving our services.\n\n"
                    + "Here are the details of your feedback:\n\n" + "%s\n\n" 
                    + "We appreciate your contribution and look forward to hearing more from you.\n\n"
                    + "Best Regards,\n" + "Support Team", feedbackDetails);
			sendNotification(userEmail, userSubject, userText);
			
			// Email to HOD
			String assignedSubject = "New Feedback Received – Action Required";
			String assignedText = String.format("Dear HOD,\n\n"
			                    + "A new feedback has been received and needs your review and consideration. Please find the details below:\n\n"
			                    + "Feedback Details:\n%s\n\n"
			                    + "Kindly review the feedback and take appropriate action as required. Please keep the user informed of the status.\n\n"
			                    + "Best Regards,\n" + "Support Team", feedbackDetails);
			sendNotification(HODEmail, assignedSubject, assignedText);
			
			// Email to assigned person
			String assignedPersonSubject = "New Feedback Assigned – Action Required";
			String assignedPersonText = String.format("Dear Assigned Person,\n\n"
			                    + "A new feedback has been assigned to you for review and action. Please find the details below:\n\n"
			                    + "Feedback Details:\n%s\n\n"
			                    + "Kindly review the feedback and take necessary action. Please ensure the user is updated on the status.\n\n"
			                    + "Best Regards,\n" + "Support Team", feedbackDetails);
			sendNotification(assignedEmail, assignedPersonSubject, assignedPersonText);

			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseMessage("Success", "Emails sent successfully", null));
		}
		catch(Exception e) {
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

		// Use string concatenation
		return "Complaint ID: " + complaintId + "\n" + "Title: " + subject + "\n" + "Priority: " + priority + "\n"
				+ "Description: " + description;
	}
	
	public String buildSuggestiondetails(Suggestion suggestion) {
		
		if(suggestion == null) {
			return "Suggestion not found";
		}
		
		String suggestionId = suggestion.getSuggestion_id() != null ? suggestion.getSuggestion_id() : "N/A";
		String subject = suggestion.getSubject() != null ? suggestion.getSubject() : "N/A";
		String description = suggestion.getDescription() != null ? suggestion.getDescription() : "N/A";
		
		return "Suggestion Id:" + suggestionId +"\n" + "Title: " + subject +"\n" + "Description: " + description;
	}
	
	public String buildFeedbackDetails(Feedback feedback) {
		
	    if(feedback == null) {
	        return "Feedback not found";
	    }
			
	    String feedbackId = feedback.getFeedback_id() != null ? feedback.getFeedback_id() : "N/A";
	    String subject = feedback.getSubject() != null ? feedback.getSubject() : "N/A";
	    String description = feedback.getDescription() != null ? feedback.getDescription() : "N/A";
	    String rating = String.valueOf(feedback.getRating() != 0 ? feedback.getRating() : "N/A");
			
	    return "Feedback Id: " + feedbackId + "\n" 
	        + "Subject: " + subject + "\n" 
	        + "Description: " + description + "\n"
	        + "Rating: " + rating;
	}
	

}
