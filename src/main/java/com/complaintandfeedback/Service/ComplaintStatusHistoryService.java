package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.ComplaintStatusHistory;
import com.complaintandfeedback.Model.ResponseMessage;
@Service
public class ComplaintStatusHistoryService {
	
	@Autowired
    private CommonUtils commonUtils;

	public ResponseEntity<Object> saveComplaintStatusHistory(ComplaintStatusHistory complaintStatusHistory, Connection l_DBConnection) {
	    try {
	        String complaintStatusHistoryId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	        complaintStatusHistory.setComplaint_status_history_id(complaintStatusHistoryId);

	        String insertQuery = "INSERT INTO complaint_status_history "
	        	    + "(complaint_status_history_id, complaint_id, from_status, to_status, reason, changed_by, changed_on) "
	        	    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

	        PreparedStatement pstmt = l_DBConnection.prepareStatement(insertQuery);
	        String reason = "Deferred".equalsIgnoreCase(complaintStatusHistory.getTo_status()) 
	        	    ? complaintStatusHistory.getReason() 
	        	    : null;
	        

	        pstmt.setString(1, complaintStatusHistoryId);
	        pstmt.setString(2, complaintStatusHistory.getComplaint_id());
	        pstmt.setString(3, complaintStatusHistory.getFrom_status());
	        pstmt.setString(4, complaintStatusHistory.getTo_status());
	    	pstmt.setString(5, reason);
	        pstmt.setString(6, complaintStatusHistory.getChanged_by());
	        pstmt.setString(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));


	        int rowsAffected = pstmt.executeUpdate();

	        if (rowsAffected > 0) {
	            return ResponseEntity.status(HttpStatus.CREATED)
	                    .body(new ResponseMessage("Success", "Complaint status saved successfully", complaintStatusHistoryId));
	        } else {
	            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save complaint status history");
	        }

	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	    }
	}


}



