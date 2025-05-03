package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.ComplaintMessageHistoryTrn;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ComplaintMessageHistoryService {

    @Autowired
    private DataSource l_DataSource;

    @Autowired
    private CommonUtils commonUtils;

    public ResponseEntity<Object> saveComplaintMessage(ComplaintMessageHistoryTrn message) {
        Connection l_DBConnection = null;

        try {
            l_DBConnection = l_DataSource.getConnection();

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
        } finally {
            if (l_DBConnection != null)
                try {
                    l_DBConnection.close();
                } catch (Exception e) {
                    return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
                }
        }
    }

    public ResponseEntity<Object> getMessagesByComplaintId(String complaintId) {
        Connection l_DBConnection = null;
        JSONArray l_DataArray = new JSONArray();

        try {
            l_DBConnection = l_DataSource.getConnection();

            String l_Query = "SELECT * FROM complaint_message_history WHERE complaint_id = ? ORDER BY sent_on ASC";
            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);
            l_PreparedStatement.setString(1, complaintId);

            ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
            l_DataArray = CommonUtils.convertToJsonArray(l_ResultSet, 0);

            if (l_DataArray.isEmpty()) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "No messages found");
            } else {
                TypeReference<List<ComplaintMessageHistoryTrn>> typeReference = new TypeReference<List<ComplaintMessageHistoryTrn>>() {};
                List<ComplaintMessageHistoryTrn> l_Data_List = new ObjectMapper().readValue(l_DataArray.toString(), typeReference);
                return ResponseEntity.status(HttpStatus.OK).body(l_Data_List);
            }

        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        } finally {
            if (l_DBConnection != null)
                try {
                    l_DBConnection.close();
                } catch (Exception e) {
                    return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
                }
        }
    }
}
