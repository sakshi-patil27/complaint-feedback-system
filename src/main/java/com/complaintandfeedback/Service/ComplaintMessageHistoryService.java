package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.ComplaintMessageHistoryTrn;
import com.complaintandfeedback.Model.ResponseMessage;

@Service
public class ComplaintMessageHistoryService {

    @Autowired
    private DataSource l_DataSource;

    @Autowired
    private CommonUtils commonUtils;

    public ResponseEntity<Object> saveComplaintMessage(ComplaintMessageHistoryTrn message,Connection l_DBConnection) {

        try {


            String messageId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            message.setComplaint_message_history_id(messageId);

            String l_Query = "INSERT INTO complaint_message_history (complaint_message_history_id, complaint_id, sender_id, receiver_id, message, sent_on) "
                           + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);
            l_PreparedStatement.setString(1, messageId);
            l_PreparedStatement.setString(2, message.getComplaint_id());
            l_PreparedStatement.setString(3, message.getSender_id());
            l_PreparedStatement.setString(4, message.getReceiver_id());
            l_PreparedStatement.setString(5, message.getMessage());
            l_PreparedStatement.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            int rowsAffected = l_PreparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ResponseMessage("Success", "Message saved successfully", messageId));
            } else {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save message");
            }

        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        } 
    }

    public ResponseEntity<Object> getMessagesByComplaintId(String complaintId) {
        Connection l_DBConnection = null;
        JSONArray l_DataArray = new JSONArray();

        try {
            l_DBConnection = l_DataSource.getConnection();

            String l_Query = "SELECT c.complaint_message_history_id, c.complaint_id, c.sender_id, c.receiver_id, " +
                             "c.message, c.sent_on, " +
                             "sender.name AS l_sender_id, receiver.name AS l_receiver_id, " +
                             "senderRole.role_name AS l_sender_role, receiverRole.role_name AS l_receiver_role, " +
                             "a.attachment_id, a.file_path, a.stored_file_name, a.uploaded_file_name, " +
                             "a.entity_type,a.entity_id,a.uploaded_by,a.uploaded_on "+
                             "FROM complaint_message_history c " +
                             "LEFT JOIN account_user_mst sender ON sender.account_id = c.sender_id " +
                             "LEFT JOIN account_user_mst receiver ON receiver.account_id = c.receiver_id " +
                             "LEFT JOIN attachment_trn a ON a.entity_id = c.complaint_message_history_id AND a.entity_type = 'COMPLAINT_MESSAGE' " +
                             "LEFT JOIN roles_mst senderRole ON senderRole.role_id = sender.role_id " +
                             "LEFT JOIN roles_mst receiverRole ON receiverRole.role_id = receiver.role_id " +
                             "WHERE c.complaint_id = ? " +
                             "ORDER BY c.sent_on ASC";

            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);
            l_PreparedStatement.setString(1, complaintId);

            ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
            l_DataArray = CommonUtils.convertToJsonArray(l_ResultSet, 0);

            if (l_DataArray.isEmpty()) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "No messages found");
            } else {
                List<Map<String, Object>> responseList = new ArrayList<>();

                for (int i = 0; i < l_DataArray.length(); i++) {
                    JSONObject jsonObject = l_DataArray.getJSONObject(i);
                    Map<String, Object> responseMap = new HashMap<>();

                    // Core message fields
                    responseMap.put("complaint_message_history_id", jsonObject.optString("complaint_message_history_id"));
                    responseMap.put("complaint_id", jsonObject.optString("complaint_id"));
                    responseMap.put("sender_id", jsonObject.optString("sender_id"));
                    responseMap.put("receiver_id", jsonObject.optString("receiver_id"));
                    responseMap.put("message", jsonObject.optString("message"));
                    responseMap.put("sent_on", jsonObject.optString("sent_on"));
                    responseMap.put("l_sender_id", jsonObject.optString("l_sender_id"));
                    responseMap.put("l_receiver_id", jsonObject.optString("l_receiver_id"));
                    responseMap.put("l_sender_role", jsonObject.optString("l_sender_role"));
                    responseMap.put("l_receiver_role", jsonObject.optString("l_receiver_role"));
                    // Attachment fields (optional)
                    if (!jsonObject.isNull("attachment_id")) {
                        Map<String, Object> attachmentMap = new HashMap<>();
                        attachmentMap.put("attachment_id", jsonObject.optString("attachment_id"));
                        attachmentMap.put("entity_type", jsonObject.optString("attachment_id"));
                        attachmentMap.put("entity_id", jsonObject.optString("attachment_id"));
                        attachmentMap.put("uploaded_by", jsonObject.optString("attachment_id"));
                        attachmentMap.put("uploaded_on", jsonObject.optString("attachment_id"));         
                        attachmentMap.put("file_path", jsonObject.optString("file_path"));
                        attachmentMap.put("stored_file_name", jsonObject.optString("stored_file_name"));
                        attachmentMap.put("uploaded_file_name", jsonObject.optString("uploaded_file_name"));
                    	String encrypted = CommonUtils.gFN_Common_Download_Data(
                    			jsonObject.optString("file_path") +  "/" + jsonObject.optString("stored_file_name"));
                    	 attachmentMap.put("l_encrypted_file",encrypted);
                        responseMap.put("attachment", attachmentMap);
                    }
                    responseList.add(responseMap);
                }

                return ResponseEntity.status(HttpStatus.OK).body(responseList);
            }

        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        } finally {
            if (l_DBConnection != null) {
                try {
                    l_DBConnection.close();
                } catch (Exception e) {
                    return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
                }
            }
        }
    }

    }
