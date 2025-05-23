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
import com.complaintandfeedback.DTO.FeedbackDto;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Model.Feedback;
import com.complaintandfeedback.Model.FeedbackQuestionResponse;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class FeedbackService {
	
	@Autowired
    private CommonUtils commonUtils;
	
	@Autowired
	private DataSource l_DataSource;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private ComplaintService complaintService;
	
	@Autowired
    private ObjectMapper objectMapper;

	public ResponseEntity<Object> saveFeedback(FeedbackDto feedbackDto) throws SQLException {
		
		Connection l_DBConnection = null;

		try {
		    l_DBConnection = l_DataSource.getConnection();
		    l_DBConnection.setAutoCommit(false);
		    // Generate a unique feedback ID (similar to complaint)
		    String feedbackId = "FB" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
		    Feedback feedback = feedbackDto.getFeedback();
		    feedback.setFeedback_id(feedbackId);  // Set the feedback ID

		    // SQL Insert query
		    String l_Query = "INSERT INTO feedback_trn (feedback_id, org_id, subject, description, rating, complaint_id, created_by, created_on, modified_on, modified_by, is_active, opr_id) "
		                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		    PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

		    // Set the parameters for the insert query
		    l_PreparedStatement.setString(1, feedback.getFeedback_id());
		    l_PreparedStatement.setLong(2, feedback.getOrg_id());  // Set org_id
		    l_PreparedStatement.setString(3, feedback.getSubject());
		    l_PreparedStatement.setString(4, feedback.getDescription());
		    l_PreparedStatement.setInt(5, feedback.getRating());
		    l_PreparedStatement.setString(6, feedback.getComplaint_id());
		    l_PreparedStatement.setString(7, feedback.getCreated_by());
		    l_PreparedStatement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		    l_PreparedStatement.setString(9, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		    l_PreparedStatement.setString(10, feedback.getModified_by());
		    l_PreparedStatement.setString(11, feedback.getIs_active());
		    l_PreparedStatement.setLong(12, feedback.getOpr_id());

		    int rowsAffected = l_PreparedStatement.executeUpdate();

		    if (rowsAffected > 0) {
		    	
		    	//get email of person who created complaint
		        ResponseEntity<Object> response = authenticationService.getUserByAccountId(feedback.getCreated_by());
		        
		        if(!response.getStatusCode().equals(HttpStatus.OK)) {
		        	l_DBConnection.rollback();
		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save feedback");
		        }
		        AccountUser accountUser = objectMapper.convertValue(response.getBody(), AccountUser.class);
		        
		        // get complaint corresponding to the complaint 
		        CommonRequestModel commonRequestModel = new CommonRequestModel();
		        commonRequestModel.setOrg_id(feedback.getOrg_id());
		        commonRequestModel.setOpr_id(feedback.getOpr_id());
		        commonRequestModel.setId(feedback.getComplaint_id());
		        commonRequestModel.setEmail(accountUser.getEmail());
		        response = complaintService.getComplaintById(commonRequestModel);
		        
		        if(!response.getStatusCode().equals(HttpStatus.OK)) {
		        	l_DBConnection.rollback();
		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save feedback");
		        }
		        
		        Complaint complaint = objectMapper.convertValue(response.getBody(),Complaint.class);
		        
		        // Get email of corresponding HOD when complaint is created
		        String sql = "SELECT a.email\n"
		        		+ "FROM account_user_mst a\n"
		        		+ "JOIN roles_mst r ON a.role_id = r.role_id\n"
		        		+ "WHERE a.department_id = ? AND r.role_name = 'HOD';";
		        
		        l_PreparedStatement = l_DBConnection.prepareStatement(sql);
		        
		        l_PreparedStatement.setString(1, complaint.getDepartment_id());
		        
		        ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
		        
		        String hodEmail = null;
		        if(l_ResultSet.next()) {
		        	hodEmail = l_ResultSet.getString("email");
		        	if(hodEmail == null || hodEmail.isBlank()) {
		        		l_DBConnection.rollback();
				    	return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Email not Found");
				    }
		        }
		        
		        AccountUser assignedTo = objectMapper.convertValue(response.getBody(), AccountUser.class);
		        
		        String feedbackDetails = emailService.buildFeedbackDetails(feedback);
		        
		        // send mail to user , hod , assigned employeee
		        
		        emailService.notifyFeedbackCreation(accountUser.getEmail(), hodEmail, assignedTo.getEmail(), feedbackDetails);
		        
		    	//Save question response 
		        List<FeedbackQuestionResponse> feedbackQuestionResponses = feedbackDto.getFeedbackQuestionResponse();		        
		        for(FeedbackQuestionResponse feedbackQuestionResponse : feedbackQuestionResponses) {
		        	
		        	String responseId = "QR" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
		        	feedbackQuestionResponse.setResponse_id(responseId);
		        	
		        	sql = "INSERT INTO feedback_question_response(response_id, feedback_id, question_id, selected_option_id, answer_text, created_on) "
		        	           + "VALUES (?, ?, ?, ?, ?, ?)";

		        	l_PreparedStatement = l_DBConnection.prepareStatement(sql);

		        	// Set the parameters for the insert query
		        	l_PreparedStatement.setString(1, responseId);
		        	l_PreparedStatement.setString(2, feedback.getFeedback_id());
		        	l_PreparedStatement.setString(3, feedbackQuestionResponse.getQuestion_id());
		        	l_PreparedStatement.setString(4, feedbackQuestionResponse.getSelected_option_id());
		        	l_PreparedStatement.setString(5, "");
		        	l_PreparedStatement.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

		        	rowsAffected = l_PreparedStatement.executeUpdate();
		        	
		        	if (rowsAffected == 0) {
		        		l_DBConnection.rollback();
				    	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save feedback");
		        	}
		        	 
		        }
		        
		        l_DBConnection.commit();
		    	return ResponseEntity.status(HttpStatus.CREATED).body(
	                    new ResponseMessage("Success", "Feedback saved successfully", feedbackId));
		    }
		    else {
		    	l_DBConnection.rollback();
		    	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save feedback");
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
	
	//Get Feedback by Complaint
	public ResponseEntity<Object> getFeedbackByComplaint(CommonRequestModel request) throws SQLException {
		
		Connection l_DBConnection = null;
		//JSONArray l_ModuleArr = new JSONArray();
		
		try {
			l_DBConnection = l_DataSource.getConnection();
			l_DBConnection.setAutoCommit(false);
			
			String sql = "SELECT f.*, "
                    + "cb.name AS l_created_by "
                    + "FROM feedback_trn f "
                    + "LEFT JOIN account_user_mst cb ON f.created_by = cb.account_id "
                    + "WHERE f.complaint_id = ? AND f.opr_id = ? AND f.org_id = ? "
                    + "AND f.is_active = 'YES'";
			
			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(
					sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			l_PreparedStatement.setString(1, request.getId());
			l_PreparedStatement.setLong(2, request.getOpr_id());
			l_PreparedStatement.setLong(3, request.getOrg_id());
			
			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();			
			Feedback feedback = null;
			
			if (l_ResultSet.next()) {
				
				feedback = new Feedback();
				
			    // Mapping result set columns to Feedback object fields
			    feedback.setFeedback_id(l_ResultSet.getString("feedback_id"));
			    feedback.setSubject(l_ResultSet.getString("subject"));
			    feedback.setDescription(l_ResultSet.getString("description"));
			    feedback.setRating(l_ResultSet.getInt("rating"));
			    feedback.setComplaint_id(l_ResultSet.getString("complaint_id"));
			    feedback.setCreated_by(l_ResultSet.getString("created_by"));
			    
			    // Mapping Timestamp fields
			    feedback.setCreated_by(l_ResultSet.getString("created_by"));
			    feedback.setModified_on(l_ResultSet.getTimestamp("modified_on"));
			    feedback.setCreated_on(l_ResultSet.getTimestamp("created_on"));
			    feedback.setModified_by(l_ResultSet.getString("modified_by"));
			    feedback.setIs_active(l_ResultSet.getString("is_active"));
			    
			    // Mapping org_id and opr_id
			    feedback.setOrg_id(l_ResultSet.getLong("org_id"));
			    feedback.setOpr_id(l_ResultSet.getLong("opr_id"));
			    feedback.setL_created_by(l_ResultSet.getString("l_created_by"));
			    
			    
			}
						
			if (feedback == null) {
				l_DBConnection.rollback();
			    return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Feedback not found");
			} else {
				FeedbackDto feedbackDto = new FeedbackDto();
				feedbackDto.setFeedback(feedback);
				
				//now get feedback question response and set it into feedback dto
				
				sql = "SELECT qr.*, "
				           + "q.question_text AS l_question_text, "
				           + "o.option_text AS l_option_text "
				           + "FROM feedback_question_response qr "
				           + "LEFT JOIN feedback_questions_mst q ON qr.question_id = q.question_id "
				           + "LEFT JOIN feedback_question_options o ON qr.selected_option_id = o.option_id "
				           + "WHERE qr.feedback_id = ?";

				l_PreparedStatement = l_DBConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
				        ResultSet.CONCUR_READ_ONLY);

				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setString(1, feedback.getFeedback_id());  // assuming you have feedbackId available

				l_ResultSet = l_PreparedStatement.executeQuery();
				JSONArray l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

				if (l_ModuleArr.isEmpty()) {
				    return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
				}
				
				TypeReference<List<FeedbackQuestionResponse>> typeReference = new TypeReference<List<FeedbackQuestionResponse>>() {
			    };
			    List<FeedbackQuestionResponse> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
			    
			    feedbackDto.setFeedbackQuestionResponse(l_data_List);
				
			    l_DBConnection.commit();
			    return ResponseEntity.status(HttpStatus.OK).body(feedbackDto);
			}
		}
		catch (Exception e) {
			l_DBConnection.rollback();
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
		}

		finally {
			if (l_DBConnection != null)
				try {
					l_DBConnection.close();
				} catch (Exception e) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
				}
		}
	}

}
