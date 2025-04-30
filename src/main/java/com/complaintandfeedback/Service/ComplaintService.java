package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Model.ComplaintStatusHistory;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ComplaintService {
	
	@Autowired
	private ComplaintStatusHistoryService complaintStatusHistoryService;

	@Autowired
    private CommonUtils commonUtils;
	
	@Autowired
	private DataSource l_DataSource;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	// Save Complaint
	public ResponseEntity<Object> saveComplaint(Complaint complaint) throws SQLException {
	    
	    Connection l_DBConnection = null;
	    
	    try {
	        l_DBConnection = l_DataSource.getConnection();
	        l_DBConnection.setAutoCommit(false);
	        String complaintId = "COMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	        complaint.setComplaint_id(complaintId);

	        // SQL Insert query
	        String l_Query = "INSERT INTO complaint_trn (complaint_id, org_id, subject, description, priority, status, department_id, created_by, assigned_to, created_on, modified_on, modified_by, due_date, is_active ,opr_id) "
	                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	        PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

	        // Set the parameters for the insert query
	        l_PreparedStatement.setString(1, complaint.getComplaint_id());
	        l_PreparedStatement.setLong(2, complaint.getOrg_id());
	        l_PreparedStatement.setString(3, complaint.getSubject());
	        l_PreparedStatement.setString(4, complaint.getDescription());
	        l_PreparedStatement.setString(5, complaint.getPriority());
	        l_PreparedStatement.setString(6, complaint.getStatus());
	        l_PreparedStatement.setString(7, complaint.getDepartment_id());
	        l_PreparedStatement.setString(8, complaint.getCreated_by());
	        l_PreparedStatement.setString(9, complaint.getAssigned_to());
	        l_PreparedStatement.setString(10, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        l_PreparedStatement.setString(11, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        l_PreparedStatement.setString(12, complaint.getModified_by());	   
	        
	        if (complaint.getDue_date() != null) {
	            l_PreparedStatement.setString(13, complaint.getDue_date().toLocalDateTime().format(formatter));
	        } else {
	            String tomorrow = LocalDateTime.now().plusDays(1).format(formatter);
	            l_PreparedStatement.setString(13, tomorrow);
	        }
	        l_PreparedStatement.setString(14,complaint.getIs_active());
	        l_PreparedStatement.setLong(15, complaint.getOpr_id());

	        int rowsAffected = l_PreparedStatement.executeUpdate();
	        
	        if (rowsAffected > 0) {
	        	
	        	//Also creating a record in the Complaint status history table
		        
		        ComplaintStatusHistory complaintService = new ComplaintStatusHistory();
		        
		        complaintService.setComplaint_id(complaintId);
		        complaintService.setFrom_status("");
		        complaintService.setTo_status(complaint.getStatus());
		        complaintService.setReason("");		        
		        complaintService.setChanged_by(complaint.getCreated_by());		        
		        
		        ResponseEntity<Object> response =complaintStatusHistoryService.saveComplaintStatusHistory(complaintService,l_DBConnection);
		        
		        if(!response.getStatusCode().equals(HttpStatus.CREATED)) {
		        	l_DBConnection.rollback();
		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save complaint");
		        }
	        	
		        l_DBConnection.commit();
	            return ResponseEntity.status(HttpStatus.CREATED).body(
	                    new ResponseMessage("Success", "Complaint saved successfully", complaintId)
	            );
	        } else {
	        	l_DBConnection.rollback();
	            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save complaint");
	        }
	        	        
	    } catch (Exception e) {
	    	l_DBConnection.rollback();
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
	    } finally {
	        if (l_DBConnection != null) {
	            try {
	                l_DBConnection.close();
	            } catch (Exception e) {
	            	l_DBConnection.rollback();
	                return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
	            }
	        }
	    }
	}
	
	// function to update status
	public ResponseEntity<Object> update(CommonRequestModel request) {
	
	// Get All Complaints According to  user 
	public ResponseEntity<Object> getAllActiveDepartments(CommonRequestModel request) {
		
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();
		
		try {
			// Get role of the user 
			l_DBConnection = l_DataSource.getConnection();
			
			String l_Query = "SELECT r.role_name, u.department_id " +
	                 "FROM account_user_mst u " +
	                 "JOIN roles_mst r ON u.role_id = r.role_id " +
	                 "WHERE u.account_id = ?";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(
			        l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
			// Use parameter binding to avoid SQL injection
			l_PreparedStatement.setString(1, request.getId());
		
			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();		
			
			String roleName = null;
			String departmentId = null;
			//String accountId = null;

			if (l_ResultSet.next()) {
			    roleName = l_ResultSet.getString("role_name");
			    departmentId = l_ResultSet.getString("department_id");
			    if(roleName == null || roleName.isBlank()) {
			    	return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Role not Found");
			    }
			    if(departmentId == null || departmentId.isBlank()) {
			    	return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Department not Found");
			    }
			}
			
			//For Admin role all the complaints are visible
			if("ADMIN".equals(roleName)) {
				l_Query = "SELECT * FROM complaint_trn WHERE is_active = 'YES' AND "
						+ "org_id = ? AND opr_id = ?";
				l_PreparedStatement = l_DBConnection.prepareStatement(
				        l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrgId());
				l_PreparedStatement.setLong(2, request.getOprId());
				
				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
				
				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"NO DATA FOUND");
				} else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
							typeReference);
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}
						
			//For HOD user
			if("HOD".equals(roleName)) {
				l_Query = "SELECT * FROM complaint_trn WHERE is_active = 'YES' AND "
						+ "org_id = ? AND opr_id = ? AND department_id = ?";  
				l_PreparedStatement = l_DBConnection.prepareStatement(
				        l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrgId());
				l_PreparedStatement.setLong(2, request.getOprId());
				l_PreparedStatement.setString(3, departmentId);
				
				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
				
				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"NO DATA FOUND");
				} 
				else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
							typeReference);
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}
						
			
			//For Employee	
			if("CLIENT".equals(roleName)) {
				l_Query = "SELECT * FROM complaint_trn WHERE is_active = 'YES' AND "
						+ "org_id = ? AND opr_id = ? AND (created_by = ? OR assigned_to = ?)" ;
				l_PreparedStatement = l_DBConnection.prepareStatement(
				        l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrgId());
				l_PreparedStatement.setLong(2, request.getOprId());
				l_PreparedStatement.setString(3, request.getId());
				
				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
				
				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"NO DATA FOUND");
				} 
				else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
							typeReference);
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}
			
			
			
		}
		
		catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
		}

		finally {
			if (l_DBConnection != null)
				try {
					l_DBConnection.close();
				} catch (Exception e) {
					return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
				}
		}	
		
		return null;
	}
	
		

}
