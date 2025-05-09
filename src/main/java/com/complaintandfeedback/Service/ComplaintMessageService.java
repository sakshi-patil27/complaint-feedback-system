package com.complaintandfeedback.Service;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.sql.DataSource;

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

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class ComplaintMessageService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private ComplaintMessageHistoryService complaintMessageHistoryService;

    @Autowired
    private DataSource l_DataSource;

    @Autowired
    private JavaMailSender mailSender;


    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Handles sending a complaint message and notifying the receiver by email.
     * Also handles storing the message, optional file attachments, and sending email with attachments.
     *
     * @param req Complaint message request including sender, receiver, message, and optional attachments
     * @return ResponseEntity with status and message
     */
    public ResponseEntity<Object> sendMessageAndNotify(ComplaintMessageRequest req) {
        Connection l_DBConnection = null;
        try {
            // Start DB transaction
            l_DBConnection = l_DataSource.getConnection();
            l_DBConnection.setAutoCommit(false);

            // Step 1: Fetch receiver's user details
            ResponseEntity<Object> receiverResponse = authenticationService.getUserByAccountId(req.getReceiverId());
            if (!receiverResponse.getStatusCode().equals(HttpStatus.OK) || receiverResponse.getBody() == null) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to retrieve receiver details");
            }
            AccountUser receiver = objectMapper.convertValue(receiverResponse.getBody(), AccountUser.class);
            if (receiver.getEmail() == null || receiver.getEmail().isBlank()) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Receiver email is missing");
            }

            // Step 2: Fetch sender's user details
            ResponseEntity<Object> senderResponse = authenticationService.getUserByAccountId(req.getSenderId());
            if (!senderResponse.getStatusCode().equals(HttpStatus.OK) || senderResponse.getBody() == null) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to retrieve sender details");
            }
            AccountUser sender = objectMapper.convertValue(senderResponse.getBody(), AccountUser.class);
            if (sender.getEmail() == null || sender.getEmail().isBlank()) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Sender email is missing");
            }

            // Step 3: Create and configure email
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true); // true for multipart (attachments)
            helper.setTo(receiver.getEmail());
            helper.setFrom(new InternetAddress("noreply@yourdomain.com", sender.getEmail()));
            helper.setReplyTo(sender.getEmail());
            helper.setSubject("New message regarding complaint: " + req.getComplaintId());
            helper.setText("Message: " + req.getMessage(), false);

            // Step 4: Save message to complaint_message_history_trn table
            ComplaintMessageHistoryTrn msg = new ComplaintMessageHistoryTrn();
            msg.setComplaint_message_history_id(UUID.randomUUID().toString().substring(0, 16));
            msg.setComplaint_id(req.getComplaintId());
            msg.setSender_id(req.getSenderId());
            msg.setReceiver_id(req.getReceiverId());
            msg.setMessage(req.getMessage());
            msg.setSent_on(Timestamp.valueOf(LocalDateTime.now()));

            ResponseEntity<Object> response = complaintMessageHistoryService.saveComplaintMessage(msg, l_DBConnection);
            if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                l_DBConnection.rollback();
                return response;
            }

            // Step 5: If there is an attachment, set its entity info and save it
            if (req.getAttachmentTrn() != null) {
                ResponseMessage respMsg = (ResponseMessage) response.getBody();
                req.getAttachmentTrn().setEntity_id((String) respMsg.getId()); // Link to message ID
                req.getAttachmentTrn().setEntity_type("COMPLAINT_MESSAGE");

                response = commonUtils.saveAttachment(req.getAttachmentTrn(), l_DBConnection);
                if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
                    l_DBConnection.rollback();
                    return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save attachments");
                }
            }

            // Step 6: Attach uploaded file(s) to email
            if (req.getAttachmentTrn() != null) {
                String query = "SELECT file_path, stored_file_name FROM attachment_trn "
                             + "WHERE entity_type = 'COMPLAINT_MESSAGE' AND attachment_id = ?";
                PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(
                        query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                ResponseMessage respMsg = (ResponseMessage) response.getBody();
                l_PreparedStatement.setString(1, (String) respMsg.getId());

                ResultSet rs = l_PreparedStatement.executeQuery();
                while (rs.next()) {
                    String path = rs.getString("file_path");
                    String storedFileName = rs.getString("stored_file_name");

                    File file = new File(path, storedFileName);
                    if (file.exists()) {
                        helper.addAttachment(file.getName(), file); // Attach file to email
                    } 
                }
            }

            // Step 7: Send the composed email
            mailSender.send(mime);

            // Step 8: Commit DB transaction
            l_DBConnection.commit();

            // Step 9: Return success response
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseMessage("Success", "Message sent and saved successfully", null));

        } catch (Exception ex) {
            // Handle error and rollback
            try {
                if (l_DBConnection != null) l_DBConnection.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return commonUtils.responseErrorHeader(ex, "Service", HttpStatus.INTERNAL_SERVER_ERROR, null);
        } finally {
            // Ensure connection is closed
            try {
                if (l_DBConnection != null) l_DBConnection.close();
            } catch (Exception closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    /**
     * Fetches all complaint messages related to the given complaint ID.
     *
     * @param complaintId The ID of the complaint
     * @return List of complaint message history records
     */
    public ResponseEntity<Object> getMessages(String complaintId) {
        return complaintMessageHistoryService.getMessagesByComplaintId(complaintId);
    }
}
