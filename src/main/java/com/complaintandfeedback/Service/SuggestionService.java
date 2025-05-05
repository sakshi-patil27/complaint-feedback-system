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
import com.complaintandfeedback.DTO.SuggestionDto;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.Model.ResponseMessage;
import com.complaintandfeedback.Model.Suggestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class SuggestionService {
	
	@Autowired
    private CommonUtils commonUtils;
	
	@Autowired
	private DataSource l_DataSource;
	
	@Autowired
	private AttachmentService attachmentService;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
	private EmailService emailService;
	

	public ResponseEntity<Object> saveSuggestion(SuggestionDto suggestionDto) throws SQLException {
	    
	    Connection l_DBConnection = null;

	    try {
	        l_DBConnection = l_DataSource.getConnection();
	        l_DBConnection.setAutoCommit(false);
	        
	        Suggestion suggestion = suggestionDto.getSuggestion();
	        
	        // Generate a unique suggestion ID
	        String suggestionId = "SG" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
	        suggestion.setSuggestion_id(suggestionId);  // Set the suggestion ID

	        // SQL Insert query
	        String l_Query = "INSERT INTO suggestion_trn (suggestion_id, org_id, subject, description, department_id, created_by, created_on, modified_on, modified_by, attachment_id, opr_id) "
	                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	        PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

	        // Set the parameters for the insert query
	        l_PreparedStatement.setString(1, suggestion.getSuggestion_id());
	        l_PreparedStatement.setLong(2, suggestion.getOrg_id());  // Set org_id
	        l_PreparedStatement.setString(3, suggestion.getSubject());
	        l_PreparedStatement.setString(4, suggestion.getDescription());
	        l_PreparedStatement.setString(5, suggestion.getDepartment_id());
	        l_PreparedStatement.setString(6, suggestion.getCreated_by());
	        l_PreparedStatement.setString(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        l_PreparedStatement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
	        l_PreparedStatement.setString(9, suggestion.getModified_by());
	        l_PreparedStatement.setString(10, suggestion.getAttachment_id());
	        l_PreparedStatement.setLong(11, suggestion.getOpr_id());

	        int rowsAffected = l_PreparedStatement.executeUpdate();

	        if (rowsAffected > 0) {
	        	
	        	//Also check for the attachments for complaints
	        	List<AttachmentTrn> attachments = suggestionDto.getAttachments();
	        	
	        	if(!attachments.isEmpty()) {
	        		
	        		for(AttachmentTrn attachment:attachments) {
	        			attachment.setEntity_id(suggestionId);
	        		}
	        		
	        		ResponseEntity<Object> response = attachmentService.saveAttachments(attachments);
	        		if(!response.getStatusCode().equals(HttpStatus.CREATED)) {
			        	l_DBConnection.rollback();
			        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save complaint");
			        }
	        	}
	        	
	        	// Send Email to concerned parties
	        	
	        	//get email of person who created complaint
		        ResponseEntity<Object> response = authenticationService.getUserByAccountId(suggestion.getCreated_by());
		        
		        if(!response.getStatusCode().equals(HttpStatus.OK)) {
		        	l_DBConnection.rollback();
		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save Suggestion");
		        }
		        AccountUser accountUser = objectMapper.convertValue(response.getBody(), AccountUser.class);
		        
		        // Get email of corresponding HOD when complaint is created
		        l_Query = "SELECT a.email\n"
		        		+ "FROM account_user_mst a\n"
		        		+ "JOIN roles_mst r ON a.role_id = r.role_id\n"
		        		+ "WHERE a.department_id = ? AND r.role_name = 'HOD';";
		        
		        l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);
		        
		        l_PreparedStatement.setString(1, suggestion.getDepartment_id());
		        
		        ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
		        
		        String hodEmail = null;
		        if(l_ResultSet.next()) {
		        	hodEmail = l_ResultSet.getString("email");
		        	if(hodEmail == null || hodEmail.isBlank()) {
		        		l_DBConnection.rollback();
				    	return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Email not Found");
				    }
		        }
	        	
		        String suggestionDetails = emailService.buildSuggestiondetails(suggestion);
		        
		        emailService.notifySuggestionCreation(accountUser.getEmail(), hodEmail, suggestionDetails);
		        
//		        if(!response.getStatusCode().equals(HttpStatus.OK)) {
//		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Failed to save suggestion");
//		        }
		        
		        l_DBConnection.commit();
	            return ResponseEntity.status(HttpStatus.CREATED).body(
	                new ResponseMessage("Success", "Suggestion saved successfully", suggestionId)
	            );
	        } else {
	        	l_DBConnection.rollback();
	            return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save suggestion");
	        }
	    } 
	    catch (Exception e) {
	    	l_DBConnection.rollback();
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
	    } 
	    finally {
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
	
	//Get suggestion listing according to the role
	public ResponseEntity<Object> getAllSuggestion(CommonRequestModel request) {

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

	        if (l_ResultSet.next()) {
	            roleName = l_ResultSet.getString("role_name");
	            departmentId = l_ResultSet.getString("department_id");
	            if(roleName == null || roleName.isBlank()) {
	            	System.out.println(roleName);
	                return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Role not Found");
	            }
	            if(departmentId == null || departmentId.isBlank()) {
	                return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Department not Found");
	            }
	        }

	        // For Admin role, all the suggestions are visible
	        if("ADMIN".equals(roleName)) {
	        	l_Query = "SELECT s.*, "
	                    + "d.department_name AS l_department_name, "
	                    + "cb.name AS l_created_by "
	                    + "FROM suggestion_trn s "
	                    + "LEFT JOIN departments_mst d ON s.department_id = d.department_id "
	                    + "LEFT JOIN account_user_mst cb ON s.created_by = cb.account_id "
	                    + "WHERE s.org_id = ? AND s.opr_id = ?";
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
	                TypeReference<List<Suggestion>> typeReference = new TypeReference<List<Suggestion>>() {};
	                List<Suggestion> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
	                        typeReference);
	                return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
	            }
	        }

	        // For HOD and Employee
	        if("HOD".equals(roleName) || "EMPLOYEE".equals(roleName)) {
	        	l_Query = "SELECT s.*, "
	                    + "d.department_name AS l_department_name, "
	                    + "cb.name AS l_created_by "
	                    + "FROM suggestion_trn s "
	                    + "LEFT JOIN departments_mst d ON s.department_id = d.department_id "
	                    + "LEFT JOIN account_user_mst cb ON s.created_by = cb.account_id "
	                    + "WHERE s.org_id = ? AND s.opr_id = ? AND s.department_id = ?";
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
	            } else {
	                TypeReference<List<Suggestion>> typeReference = new TypeReference<List<Suggestion>>() {};
	                List<Suggestion> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
	                        typeReference);
	                return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
	            }
	        }

	        // For Client role
	        if("CLIENT".equals(roleName)) {
	        	l_Query = "SELECT s.*, "
	                    + "d.department_name AS l_department_name, "
	                    + "cb.name AS l_created_by "
	                    + "FROM suggestion_trn s "
	                    + "LEFT JOIN departments_mst d ON s.department_id = d.department_id "
	                    + "LEFT JOIN account_user_mst cb ON s.created_by = cb.account_id "
	                    + "WHERE s.org_id = ? AND s.opr_id = ? AND s.created_by = ?";
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
	            } else {
	                TypeReference<List<Suggestion>> typeReference = new TypeReference<List<Suggestion>>() {};
	                List<Suggestion> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
	                        typeReference);
	                return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
	            }
	        }

	        return commonUtils.responseErrorHeader(null, null, HttpStatus.FORBIDDEN, "Unauthorized role or role not handled");

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
	
	//Get suggestion by id
	public ResponseEntity<Object> getSuggestionById(CommonRequestModel request) {
		
		Connection l_DBConnection = null;

	    try {
	        l_DBConnection = l_DataSource.getConnection();

	        // SQL query to fetch suggestion details by suggestion_id, opr_id, org_id, and is_active = 'YES'
	        String sql = "SELECT s.*, "
                    + "d.department_name AS l_department_name, "
                    + "cb.name AS l_created_by "
                    + "FROM suggestion_trn s "
                    + "LEFT JOIN departments_mst d ON s.department_id = d.department_id "
                    + "LEFT JOIN account_user_mst cb ON s.created_by = cb.account_id "
                    + "WHERE s.suggestion_id = ? AND s.opr_id = ? AND s.org_id = ?";

	        PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(
	                sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

	        // Use parameter binding to avoid SQL injection
	        l_PreparedStatement.setString(1, request.getId());
	        l_PreparedStatement.setLong(2, request.getOprId());
	        l_PreparedStatement.setLong(3, request.getOrgId());

	        ResultSet l_ResultSet = l_PreparedStatement.executeQuery();        
	        Suggestion suggestion = null;

	        if (l_ResultSet.next()) {
	            suggestion = new Suggestion();
	            
	            // Mapping result set columns to Suggestion object fields
	            suggestion.setSuggestion_id(l_ResultSet.getString("suggestion_id"));
	            suggestion.setOrg_id(l_ResultSet.getLong("org_id"));
	            suggestion.setSubject(l_ResultSet.getString("subject"));
	            suggestion.setDescription(l_ResultSet.getString("description"));
	            suggestion.setDepartment_id(l_ResultSet.getString("department_id"));
	            suggestion.setCreated_by(l_ResultSet.getString("created_by"));
	            suggestion.setCreated_on(l_ResultSet.getTimestamp("created_on"));
	            suggestion.setModified_on(l_ResultSet.getTimestamp("modified_on"));	            
	            suggestion.setModified_by(l_ResultSet.getString("modified_by"));
	            suggestion.setAttachment_id(l_ResultSet.getString("attachment_id"));
	            suggestion.setOpr_id(l_ResultSet.getLong("opr_id"));
	            suggestion.setL_department_name(l_ResultSet.getString("l_department_name"));
	            suggestion.setL_created_by(l_ResultSet.getString("l_created_by"));
	        }

	        // If no suggestion is found, return NOT_FOUND response
	        if (suggestion == null) {
	            return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Suggestion not found");
	        } else {
	            return ResponseEntity.status(HttpStatus.OK).body(suggestion);
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
