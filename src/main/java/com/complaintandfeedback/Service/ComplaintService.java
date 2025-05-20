package com.complaintandfeedback.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.DTO.ComplaintDto;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Model.ComplaintStatusHistory;
import com.complaintandfeedback.Model.ResponseMessage;
import com.complaintandfeedback.nlp.SentimentAnalysis;
import com.complaintandfeedback.webSocketConfig.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

@Service
public class ComplaintService {

	@Autowired
	private ComplaintStatusHistoryService complaintStatusHistoryService;

	@Autowired
	private CommonUtils commonUtils;

	@Autowired
	private DataSource l_DataSource;

	@Autowired
	private AttachmentService attachmentService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SentimentAnalysis sentimentAnalysis;
	
	@Autowired
	private NotificationService notificationService;
	
	RestTemplate restTemplate = new RestTemplate();

	// private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
	// HH:mm:ss");

	// Save Complaint
	public ResponseEntity<Object> saveComplaint(ComplaintDto complaintDto) throws SQLException {

		Connection l_DBConnection = null;

		try {
			l_DBConnection = l_DataSource.getConnection();
			l_DBConnection.setAutoCommit(false);

			Complaint complaint = complaintDto.getComplaint();

			String complaintId = "COMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
			complaint.setComplaint_id(complaintId);

			String l_Query = "INSERT INTO complaint_trn (complaint_id, org_id, opr_id, subject, "
					+ "description, priority, status, department_id, created_by, assigned_to, created_on, "
					+ "modified_on, modified_by, due_date, is_active, is_anonymous, category_id, tag_id, location) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

			// Set parameters for insert query
			l_PreparedStatement.setString(1, complaint.getComplaint_id());
			l_PreparedStatement.setLong(2, complaint.getOrg_id());
			l_PreparedStatement.setLong(3, complaint.getOpr_id());
			l_PreparedStatement.setString(4, complaint.getSubject());
			l_PreparedStatement.setString(5, complaint.getDescription());	

			String text = (complaint.getPriority() == null || complaint.getPriority().isBlank()) 
					? priorityBySentiment(complaint)
					: complaint.getPriority();

			l_PreparedStatement.setString(6, text);// set priority

			l_PreparedStatement.setString(7, complaint.getStatus());
			
			
			//for ai integration code for department
			if(complaint.getDepartment_id() == null || complaint.getDepartment_id().isEmpty()) {
				CommonRequestModel request=new CommonRequestModel();
				request.setOpr_id(complaint.getOpr_id());
				request.setOrg_id(complaint.getOrg_id());
			    Map<String, Object> payload = (Map<String, Object>) getAllDepartmentWithMappedCategory(request);
			    String url = "http://localhost:8000/analyze";
			    Map<String, Object> requestBody = new HashMap<>();
			    requestBody.put("text", complaint.getDescription());
			    requestBody.put("label_mapping", payload.get("label_mapping"));
			    ObjectMapper mapper = new ObjectMapper();
			    String jsonPayload = mapper.writeValueAsString(requestBody); 
		        HttpHeaders headers = new HttpHeaders();
		        headers.setContentType(MediaType.APPLICATION_JSON);
		        HttpEntity<String> request1 = new HttpEntity<>(jsonPayload, headers);
		        ResponseEntity<String> response = restTemplate.postForEntity(url, request1, String.class);
		        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
		            JsonNode jsonNode = mapper.readTree(response.getBody());
		            complaint.setDepartment_id(jsonNode.get("department_id").asText());
		            complaint.setCategory_id(jsonNode.get("category_id").asText());
		        }
			}
			
			l_PreparedStatement.setString(8, complaint.getDepartment_id());
			l_PreparedStatement.setString(9, complaint.getCreated_by());
			l_PreparedStatement.setString(10, complaint.getAssigned_to());
			l_PreparedStatement.setString(11, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			l_PreparedStatement.setString(12, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			l_PreparedStatement.setString(13, complaint.getModified_by());
			if (complaint.getDue_date() != null) {
				// Use setTimestamp for accurate handling of Timestamp type
				l_PreparedStatement.setTimestamp(14, Timestamp.valueOf(complaint.getDue_date().toLocalDateTime()));
			} else {
				// For null due_date, set tomorrow's date correctly
				LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
				l_PreparedStatement.setTimestamp(14, Timestamp.valueOf(tomorrow));
			}
			l_PreparedStatement.setString(15, complaint.getIs_active());
			l_PreparedStatement.setString(16, complaint.getIs_anonymous());
			
			//for ai integration code for category
			if(!complaint.getDepartment_id().isEmpty() && complaint.getCategory_id().isEmpty()) {
				CommonRequestModel request=new CommonRequestModel();
				request.setOpr_id(complaint.getOpr_id());
				request.setOrg_id(complaint.getOrg_id());
				request.setId(complaint.getDepartment_id());
			    Map<String, Object> payload = (Map<String, Object>) getAllDepartmentWithMappedCategory(request);
			    String url = "http://localhost:8000/analyze";
			    Map<String, Object> requestBody = new HashMap<>();
			    requestBody.put("text", complaint.getDescription());
			    requestBody.put("label_mapping", payload.get("label_mapping"));
			    ObjectMapper mapper = new ObjectMapper();
			    String jsonPayload = mapper.writeValueAsString(requestBody); 
		        HttpHeaders headers = new HttpHeaders();
		        headers.setContentType(MediaType.APPLICATION_JSON);
		        HttpEntity<String> request1 = new HttpEntity<>(jsonPayload, headers);
		        ResponseEntity<String> response = restTemplate.postForEntity(url, request1, String.class);
		        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
		            JsonNode jsonNode = mapper.readTree(response.getBody());
		            complaint.setCategory_id(jsonNode.get("category_id").asText());
		        }
			}
			
			l_PreparedStatement.setString(17, complaint.getCategory_id());
			l_PreparedStatement.setString(18, complaint.getTag_id());
			l_PreparedStatement.setString(19, complaint.getLocation());

//	        if (complaint.getDue_date() != null) {
//	            l_PreparedStatement.setString(13, complaint.getDue_date().toLocalDateTime().format(formatter));
//	        } else {
//	            String tomorrow = LocalDateTime.now().plusDays(1).format(formatter);
//	            l_PreparedStatement.setString(13, tomorrow);
//	        }
			

			int rowsAffected = l_PreparedStatement.executeUpdate();

			if (rowsAffected > 0) {

				// Also check for the attachments for complaints
				List<AttachmentTrn> attachments = complaintDto.getAttachments();

				if (!attachments.isEmpty()) {

					for (AttachmentTrn attachment : attachments) {
						attachment.setEntity_id(complaintId);
					}

					ResponseEntity<Object> response = attachmentService.saveAttachments(attachments);
					if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
						l_DBConnection.rollback();
						return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
								"Failed to save complaint");
					}
				}

				// Also creating a record in the Complaint status history table

				ComplaintStatusHistory complaintService = new ComplaintStatusHistory();

				complaintService.setComplaint_id(complaintId);
				complaintService.setFrom_status("");
				complaintService.setTo_status(complaint.getStatus());
				complaintService.setReason("");
				complaintService.setChanged_by(complaint.getCreated_by());

				ResponseEntity<Object> response = complaintStatusHistoryService
						.saveComplaintStatusHistory(complaintService, l_DBConnection);

				if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"Failed to save complaint");
				}

				// send Mail to concerned parties

				// get email of person who created complaint
				response = authenticationService.getUserByAccountId(complaint.getCreated_by());

				if (!response.getStatusCode().equals(HttpStatus.OK)) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"Failed to save complaint");
				}
				AccountUser accountUser = objectMapper.convertValue(response.getBody(), AccountUser.class);

				// Get email of corresponding HOD when complaint is created
				l_Query = "SELECT a.email\n" + "FROM account_user_mst a\n"
						+ "JOIN roles_mst r ON a.role_id = r.role_id\n"
						+ "WHERE a.department_id = ? AND r.role_name = 'HOD';";

				l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

				l_PreparedStatement.setString(1, complaint.getDepartment_id());

				ResultSet l_ResultSet = l_PreparedStatement.executeQuery();

				String hodEmail = null;
				if (l_ResultSet.next()) {
					hodEmail = l_ResultSet.getString("email");
					if (hodEmail == null || hodEmail.isBlank()) {
						return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Email not Found");
					}
				}

				String complaintDetails = emailService.buildComplaintDetails(complaint);

				emailService.notifyComplaintCreation(accountUser.getEmail(), hodEmail, complaintDetails);

//		        if(!response.getStatusCode().equals(HttpStatus.OK)) {
//		        	l_DBConnection.rollback();
//		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save complaint");
//		        }

				l_DBConnection.commit();
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(new ResponseMessage("Success", "Complaint saved successfully", complaintId));
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

	// Update existing complaint
	public ResponseEntity<Object> updateComplaint(@Valid Complaint complaint) throws SQLException {

		Connection l_DBConnection = null;
		try {

			l_DBConnection = l_DataSource.getConnection();
			l_DBConnection.setAutoCommit(false);

			// BLV for complaint update
			if ("ASSIGNED".equals(complaint.getStatus())
					&& (complaint.getAssigned_to() == null || complaint.getAssigned_to().isEmpty())) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"Assigned to field cannot be empty when status is ASSIGNED");
			}

			String sql = "UPDATE complaint_trn SET subject = ?, description = ?, priority = ?, status = ?, "
					+ "department_id = ?, assigned_to = ?, modified_by = ?, modified_on = ?, due_date = ?, "
					+ "is_active = ?, opr_id = ?, org_id = ?, category_id = ?, tag_id = ?, location = ? " 
					+ "WHERE complaint_id = ? ";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(sql);

			l_PreparedStatement.setString(1, complaint.getSubject());
			l_PreparedStatement.setString(2, complaint.getDescription());
			l_PreparedStatement.setString(3, complaint.getPriority());
			l_PreparedStatement.setString(4, complaint.getStatus());
			l_PreparedStatement.setString(5, complaint.getDepartment_id());
			l_PreparedStatement.setString(6, complaint.getAssigned_to());
			l_PreparedStatement.setString(7, complaint.getModified_by());
			l_PreparedStatement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			l_PreparedStatement.setTimestamp(9, complaint.getDue_date());
			l_PreparedStatement.setString(10, complaint.getIs_active());
			l_PreparedStatement.setLong(11, complaint.getOpr_id());
			l_PreparedStatement.setLong(12, complaint.getOrg_id());
			l_PreparedStatement.setString(13, complaint.getCategory_id());
			l_PreparedStatement.setString(14, complaint.getTag_id());
			l_PreparedStatement.setString(15, complaint.getLocation());
			l_PreparedStatement.setString(16, complaint.getComplaint_id());

			int rowsAffected = l_PreparedStatement.executeUpdate();

			if (rowsAffected > 0) {

				// Also creating a record in the Complaint status history table

				if (!complaint.getStatus().equals(complaint.getL_previous_status())) {
					ComplaintStatusHistory complaintService = new ComplaintStatusHistory();

					complaintService.setComplaint_id(complaint.getComplaint_id());
					complaintService.setFrom_status(complaint.getL_previous_status());
					complaintService.setTo_status(complaint.getStatus());
					complaintService.setReason(complaint.getL_deffered_reason());
					complaintService.setChanged_by(complaint.getModified_by());

					ResponseEntity<Object> response = complaintStatusHistoryService
							.saveComplaintStatusHistory(complaintService, l_DBConnection);

					if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
						l_DBConnection.rollback();
						return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
								"Failed to update complaint");
					}
				}

				// send Mail to concerned parties

				// get email of person who created complaint
				ResponseEntity<Object> response = authenticationService.getUserByAccountId(complaint.getCreated_by());

				if (!response.getStatusCode().equals(HttpStatus.OK)) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"Failed to update complaint");
				}
				AccountUser accountUser = objectMapper.convertValue(response.getBody(), AccountUser.class);

				// get email of assigned to
				if (complaint.getAssigned_to() != null && !complaint.getAssigned_to().isEmpty()) {
					response = authenticationService.getUserByAccountId(complaint.getAssigned_to());

					if (!response.getStatusCode().equals(HttpStatus.OK)) {
						l_DBConnection.rollback();
						return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
								"Failed to update complaint");
					}
				}

				AccountUser assignedTo = objectMapper.convertValue(response.getBody(), AccountUser.class);

				String complaintDetails = emailService.buildComplaintDetails(complaint);

				response = emailService.notifyComplaintUpdate(accountUser.getEmail(), assignedTo.getEmail(),
						complaint.getL_previous_status(), complaint.getStatus(), complaintDetails);
				
				//send notification to the assigned user 
				if ("ASSIGNED".equals(complaint.getStatus())){
				notificationService.sendNotification(
						assignedTo.getAccount_id(),
					    "A complaint has been assigned to you: " + complaint.getSubject() 
					);
				}

//		        if(!response.getStatusCode().equals(HttpStatus.OK)) {
//		        	l_DBConnection.rollback();
//		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save complaint");
//		        }

				l_DBConnection.commit();
				return ResponseEntity.status(HttpStatus.OK).body(
						new ResponseMessage("Success", "Complaint Updated successfully", complaint.getComplaint_id()));
			} else {
				l_DBConnection.rollback();
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"Failed to Update Complaint");
			}
		} catch (Exception e) {
			l_DBConnection.rollback();
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

	// Update Status
	public ResponseEntity<Object> updateStatus(@Valid Complaint complaint) throws SQLException {
		Connection l_DBConnection = null;
		try {

			l_DBConnection = l_DataSource.getConnection();
			l_DBConnection.setAutoCommit(false);

			// Get previous status of complaint before updating
			String sql = "Select status from complaint_trn " + "WHERE complaint_id = ?";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(sql);

			l_PreparedStatement.setString(1, complaint.getComplaint_id());

			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();

			String previousStatus = null;
			if (l_ResultSet.next()) {
				previousStatus = l_ResultSet.getString("status");
				if (previousStatus == null || previousStatus.isBlank()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Complaint not found");
				}
			}

			sql = "UPDATE complaint_trn SET status = ? " + "WHERE complaint_id = ?";

			l_PreparedStatement = l_DBConnection.prepareStatement(sql);

			l_PreparedStatement.setString(1, complaint.getStatus());
			l_PreparedStatement.setString(2, complaint.getComplaint_id());

			int rowsAffected = l_PreparedStatement.executeUpdate();

			if (rowsAffected > 0) {

				// Also creating a record in the Complaint status history table

				ComplaintStatusHistory complaintService = new ComplaintStatusHistory();

				complaintService.setComplaint_id(complaint.getComplaint_id());
				complaintService.setFrom_status(previousStatus);
				complaintService.setTo_status(complaint.getStatus());
				complaintService.setReason("");
				complaintService.setChanged_by(complaint.getCreated_by());

				ResponseEntity<Object> response = complaintStatusHistoryService
						.saveComplaintStatusHistory(complaintService, l_DBConnection);

				if (!response.getStatusCode().equals(HttpStatus.CREATED)) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"Failed to save complaint");
				}

				// send Mail to concerned parties

				// get email of person who created complaint
				response = authenticationService.getUserByAccountId(complaint.getCreated_by());

				if (!response.getStatusCode().equals(HttpStatus.OK)) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"Failed to update complaint");
				}
				AccountUser accountUser = objectMapper.convertValue(response.getBody(), AccountUser.class);

				// Get email of corresponding HOD when complaint is created
				sql = "SELECT a.email\n" + "FROM account_user_mst a\n" + "JOIN roles_mst r ON a.role_id = r.role_id\n"
						+ "WHERE a.department_id = ? AND r.role_name = 'HOD';";

				l_PreparedStatement = l_DBConnection.prepareStatement(sql);

				l_PreparedStatement.setString(1, complaint.getDepartment_id());

				l_ResultSet = l_PreparedStatement.executeQuery();

				String hodEmail = null;
				if (l_ResultSet.next()) {
					hodEmail = l_ResultSet.getString("email");
					if (hodEmail == null || hodEmail.isBlank()) {
						return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Email not Found");
					}
				}

				// get email of assigned to
				response = authenticationService.getUserByAccountId(complaint.getAssigned_to());

				if (!response.getStatusCode().equals(HttpStatus.OK)) {
					l_DBConnection.rollback();
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"Failed to update complaint");
				}
				AccountUser assignedTo = objectMapper.convertValue(response.getBody(), AccountUser.class);

				String complaintDetails = emailService.buildComplaintDetails(complaint);

				// send mail to user and hod
				emailService.notifyComplaintUpdate(accountUser.getEmail(), hodEmail, complaint.getL_previous_status(),
						complaint.getStatus(), complaintDetails);

				// send mail to assigned to
				emailService.notifyComplaintUpdate("", assignedTo.getEmail(), complaint.getL_previous_status(),
						complaint.getStatus(), complaintDetails);

//		        if(!response.getStatusCode().equals(HttpStatus.OK) && !response1.getStatusCode().equals(HttpStatus.OK)) {
//		        	l_DBConnection.rollback();
//		        	return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to update complaint");
//		        }

				l_DBConnection.commit();
				return ResponseEntity.status(HttpStatus.OK).body(
						new ResponseMessage("Success", "Complaint Updated successfully", complaint.getComplaint_id()));
			} else {
				l_DBConnection.rollback();
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"Failed to update complaint");
			}
		} catch (Exception e) {
			l_DBConnection.rollback();
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

	// Get All Complaints According to user
	public ResponseEntity<Object> getAllActiveComplaints(CommonRequestModel request) {

		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();

		try {
			// Get role of the user
			l_DBConnection = l_DataSource.getConnection();

			String l_Query = "SELECT r.role_name, u.department_id " + "FROM account_user_mst u "
					+ "JOIN roles_mst r ON u.role_id = r.role_id " + "WHERE u.account_id = ?";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			// Use parameter binding to avoid SQL injection
			l_PreparedStatement.setString(1, request.getId());

			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();

			String roleName = null;
			String departmentId = null;
			// String accountId = null;

			if (l_ResultSet.next()) {
				roleName = l_ResultSet.getString("role_name");
				departmentId = l_ResultSet.getString("department_id");
				if (roleName == null || roleName.isBlank()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Role not Found");
				}
				if (departmentId == null || departmentId.isBlank()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Department not Found");
				}
			}

			// For Admin role all the complaints are visible
			if ("ADMIN".equals(roleName)) {
				l_Query = "SELECT c.*,"
						+ "cb.name AS l_created_by, "
						+ "at.name AS l_assigned_to, " 
						+ "d.department_name AS l_department_name, "
						+ "cat.category_name AS l_category_name "
						+ "FROM complaint_trn c "
						+ "LEFT JOIN departments_mst d ON c.department_id = d.department_id "
						+ "LEFT JOIN account_user_mst cb ON c.created_by = cb.account_id "
						+ "LEFT JOIN account_user_mst at ON c.assigned_to = at.account_id "
						+ "LEFT JOIN category_mst cat ON c.category_id = cat.category_id "
						+ "WHERE c.is_active = 'YES' AND "
						+ "c.org_id = ? AND c.opr_id = ? AND c.department_id != '0333157788020511' "
						+ "ORDER BY c.created_on DESC";
				l_PreparedStatement = l_DBConnection.prepareStatement(l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrg_id());
				l_PreparedStatement.setLong(2, request.getOpr_id());

				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
				} else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
					for (Complaint complaint : l_data_List) {
						l_Query = "SELECT COUNT(*) as count " + "FROM feedback_trn f "
								+ "WHERE f.is_active = 'YES' AND f.complaint_id = ?";

						l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

						l_PreparedStatement.setString(1, complaint.getComplaint_id());
						ResultSet l_ResultSet1 = l_PreparedStatement.executeQuery();

						int count = 0;
						if (l_ResultSet1.next()) {
							count = l_ResultSet1.getInt("count");
						}
						complaint.setHas_feedback(count > 0);

						String[] tagIdArray = complaint.getTag_id().split(",\\s*");
						if (tagIdArray != null || complaint.getTag_id().isEmpty()) {
							String tagId = "";
							for (int i = 0; i < tagIdArray.length; i++) {
								if (i > 0) {
									tagId += ",";
								}
								tagId += "'" + tagIdArray[i] + "'"; 
							}

							String sql = "SELECT tag_name FROM tags_mst WHERE tag_id IN (" + tagId + ")";

							l_PreparedStatement = l_DBConnection.prepareStatement(sql);
							ResultSet l_ResultSet2 = l_PreparedStatement.executeQuery();
							StringBuilder tagNamesBuilder = new StringBuilder();
							while (l_ResultSet2.next()) {
							    if (tagNamesBuilder.length() > 0) {
							        tagNamesBuilder.append(",");
							    }
							    tagNamesBuilder.append(l_ResultSet2.getString("tag_name"));
							}

							String tagNamesCsv = tagNamesBuilder.toString();
							complaint.setL_tag_name(tagNamesCsv);

						}
					}
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}

			// For HOD user
			if ("HOD".equals(roleName)) {
				l_Query = "SELECT c.*, " + "d.department_name AS l_department_name, " 
						+ "cb.name AS l_created_by, "
						+ "at.name AS l_assigned_to, " 
						+ "cat.category_name AS l_category_name "
						+ "FROM complaint_trn c "
						+ "LEFT JOIN departments_mst d ON c.department_id = d.department_id "
						+ "LEFT JOIN account_user_mst cb ON c.created_by = cb.account_id "
						+ "LEFT JOIN account_user_mst at ON c.assigned_to = at.account_id "
						+ "LEFT JOIN category_mst cat ON c.category_id = cat.category_id "
						+ "WHERE c.is_active = 'YES' " + "AND c.org_id = ? AND c.opr_id = ? AND c.department_id = ? "
						+ "OR c.created_by = ?"
						+ "ORDER BY created_on DESC";
				l_PreparedStatement = l_DBConnection.prepareStatement(l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrg_id());
				l_PreparedStatement.setLong(2, request.getOpr_id());
				l_PreparedStatement.setString(3, departmentId);
				l_PreparedStatement.setString(4, request.getId());
				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
				} else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
					for (Complaint complaint : l_data_List) {
						l_Query = "SELECT COUNT(*) as count " + "FROM feedback_trn f "
								+ "WHERE f.is_active = 'YES' AND f.complaint_id = ?";

						l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

						l_PreparedStatement.setString(1, complaint.getComplaint_id());
						ResultSet l_ResultSet1 = l_PreparedStatement.executeQuery();

						int count = 0;
						if (l_ResultSet1.next()) {
							count = l_ResultSet1.getInt("count");
						}
						complaint.setHas_feedback(count > 0);

						// if anonymous flag is true then set created by blank
						// Also make sure created by is not set "" when employee himself creates
						// anonymous complaint
						if ("YES".equals(complaint.getIs_anonymous())
								&& !request.getId().equals(complaint.getCreated_by())) {
							complaint.setCreated_by("");
							complaint.setL_created_by("");
						}
						
						String[] tagIdArray = complaint.getTag_id().split(",\\s*");
						if (tagIdArray != null || complaint.getTag_id().isEmpty()) {
							String tagId = "";
							for (int i = 0; i < tagIdArray.length; i++) {
								if (i > 0) {
									tagId += ",";
								}
								tagId += "'" + tagIdArray[i] + "'"; 
							}

							String sql = "SELECT tag_name FROM tags_mst WHERE tag_id IN (" + tagId + ")";

							l_PreparedStatement = l_DBConnection.prepareStatement(sql);
							ResultSet l_ResultSet2 = l_PreparedStatement.executeQuery();
							StringBuilder tagNamesBuilder = new StringBuilder();
							while (l_ResultSet2.next()) {
							    if (tagNamesBuilder.length() > 0) {
							        tagNamesBuilder.append(",");
							    }
							    tagNamesBuilder.append(l_ResultSet2.getString("tag_name"));
							}

							String tagNamesCsv = tagNamesBuilder.toString();
							complaint.setL_tag_name(tagNamesCsv);

						}

					}
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}

			// For Client
			if ("CLIENT".equals(roleName)) {
				l_Query = "SELECT c.*, " + "d.department_name AS l_department_name, " 
						+ "cb.name AS l_created_by, "
						+ "at.name AS l_assigned_to, " 
						+ "cat.category_name AS l_category_name "
						+ "FROM complaint_trn c "
						+ "LEFT JOIN departments_mst d ON c.department_id = d.department_id "
						+ "LEFT JOIN account_user_mst cb ON c.created_by = cb.account_id "
						+ "LEFT JOIN account_user_mst at ON c.assigned_to = at.account_id "
						+ "LEFT JOIN category_mst cat ON c.category_id = cat.category_id "
						+ "WHERE c.is_active = 'YES' " + "AND c.org_id = ? AND c.opr_id = ? AND c.created_by = ? "
						+ "ORDER BY created_on DESC";
				l_PreparedStatement = l_DBConnection.prepareStatement(l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrg_id());
				l_PreparedStatement.setLong(2, request.getOpr_id());
				l_PreparedStatement.setString(3, request.getId());

				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
				} else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
					for (Complaint complaint : l_data_List) {
						l_Query = "SELECT COUNT(*) as count " + "FROM feedback_trn f "
								+ "WHERE f.is_active = 'YES' AND f.complaint_id = ?";

						l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

						l_PreparedStatement.setString(1, complaint.getComplaint_id());
						ResultSet l_ResultSet1 = l_PreparedStatement.executeQuery();

						int count = 0;
						if (l_ResultSet1.next()) {
							count = l_ResultSet1.getInt("count");
						}
						complaint.setHas_feedback(count > 0);

						// if anonymous flag is true then set created by blank
						// Also make sure created by is not set "" when employee himself creates
						// anonymous complaint
						if ("YES".equals(complaint.getIs_anonymous())
								&& !request.getId().equals(complaint.getCreated_by())) {
							complaint.setCreated_by("");
							complaint.setL_created_by("");
						}
						
						String[] tagIdArray = complaint.getTag_id().split(",\\s*");
						if (tagIdArray != null || complaint.getTag_id().isEmpty()) {
							String tagId = "";
							for (int i = 0; i < tagIdArray.length; i++) {
								if (i > 0) {
									tagId += ",";
								}
								tagId += "'" + tagIdArray[i] + "'"; 
							}

							String sql = "SELECT tag_name FROM tags_mst WHERE tag_id IN (" + tagId + ")";

							l_PreparedStatement = l_DBConnection.prepareStatement(sql);
							ResultSet l_ResultSet2 = l_PreparedStatement.executeQuery();
							StringBuilder tagNamesBuilder = new StringBuilder();
							while (l_ResultSet2.next()) {
							    if (tagNamesBuilder.length() > 0) {
							        tagNamesBuilder.append(",");
							    }
							    tagNamesBuilder.append(l_ResultSet2.getString("tag_name"));
							}

							String tagNamesCsv = tagNamesBuilder.toString();
							complaint.setL_tag_name(tagNamesCsv);

						}
												
					}
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}

			// For employee
			if ("EMPLOYEE".equals(roleName)) {
				l_Query = "SELECT c.*, " + "d.department_name AS l_department_name, " 
						+ "cb.name AS l_created_by, "
						+ "at.name AS l_assigned_to, " 
						+ "cat.category_name AS l_category_name "
						+ "FROM complaint_trn c "
						+ "LEFT JOIN departments_mst d ON c.department_id = d.department_id "
						+ "LEFT JOIN account_user_mst cb ON c.created_by = cb.account_id "
						+ "LEFT JOIN account_user_mst at ON c.assigned_to = at.account_id "
						+ "LEFT JOIN category_mst cat ON c.category_id = cat.category_id "
						+ "WHERE c.is_active = 'YES' " + "AND c.org_id = ? AND c.opr_id = ? "
						+ "AND (c.created_by = ? OR c.assigned_to = ?) " + "ORDER BY created_on DESC";
				l_PreparedStatement = l_DBConnection.prepareStatement(l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				// Use parameter binding to avoid SQL injection
				l_PreparedStatement.setLong(1, request.getOrg_id());
				l_PreparedStatement.setLong(2, request.getOpr_id());
				l_PreparedStatement.setString(3, request.getId());
				l_PreparedStatement.setString(4, request.getId());

				l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
				} else {
					TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
					};
					List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
					for (Complaint complaint : l_data_List) {
						l_Query = "SELECT COUNT(*) as count " + "FROM feedback_trn f "
								+ "WHERE f.is_active = 'YES' AND f.complaint_id = ?";

						l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
								ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

						l_PreparedStatement.setString(1, complaint.getComplaint_id());
						ResultSet l_ResultSet1 = l_PreparedStatement.executeQuery();

						int count = 0;
						if (l_ResultSet1.next()) {
							count = l_ResultSet1.getInt("count");
						}
						complaint.setHas_feedback(count > 0);

						// if anonymous flag is true then set created by blank
						// Also make sure created by is not set "" when employee himself creates
						// anonymous complaint
						if ("YES".equals(complaint.getIs_anonymous())
								&& !request.getId().equals(complaint.getCreated_by())) {
							complaint.setCreated_by("");
							complaint.setModified_by("");
							complaint.setL_created_by("");
						}

						String[] tagIdArray = complaint.getTag_id().split(",\\s*");
						if (tagIdArray != null || complaint.getTag_id().isEmpty()) {
							String tagId = "";
							for (int i = 0; i < tagIdArray.length; i++) {
								if (i > 0) {
									tagId += ",";
								}
								tagId += "'" + tagIdArray[i] + "'"; 
							}

							String sql = "SELECT tag_name FROM tags_mst WHERE tag_id IN (" + tagId + ")";

							l_PreparedStatement = l_DBConnection.prepareStatement(sql);
							ResultSet l_ResultSet2 = l_PreparedStatement.executeQuery();
							StringBuilder tagNamesBuilder = new StringBuilder();
							while (l_ResultSet2.next()) {
							    if (tagNamesBuilder.length() > 0) {
							        tagNamesBuilder.append(",");
							    }
							    tagNamesBuilder.append(l_ResultSet2.getString("tag_name"));
							}

							String tagNamesCsv = tagNamesBuilder.toString();
							complaint.setL_tag_name(tagNamesCsv);

						}
						
					}
					return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
				}
			}
			return commonUtils.responseErrorHeader(null, null, HttpStatus.FORBIDDEN,
					"Unauthorized role or role not handled");
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
	}

	// get complaint by id
	public ResponseEntity<Object> getComplaintById(CommonRequestModel request) {

		Connection l_DBConnection = null;
		// JSONArray l_ModuleArr = new JSONArray();

		try {
			l_DBConnection = l_DataSource.getConnection();

			String sql = "SELECT c.*, " + "d.department_name AS l_department_name, " + "cb.name AS l_created_by, "
					+ "at.name AS l_assigned_to, " + "cat.category_name AS l_category_name " + "FROM complaint_trn c "
					+ "LEFT JOIN departments_mst d ON c.department_id = d.department_id "
					+ "LEFT JOIN account_user_mst cb ON c.created_by = cb.account_id "
					+ "LEFT JOIN account_user_mst at ON c.assigned_to = at.account_id "
					+ "LEFT JOIN category_mst cat ON c.category_id = cat.category_id "
					+ "WHERE c.complaint_id = ? AND c.opr_id = ? AND c.org_id = ? " + "AND c.is_active = 'YES'";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			l_PreparedStatement.setString(1, request.getId());
			l_PreparedStatement.setLong(2, request.getOpr_id());
			l_PreparedStatement.setLong(3, request.getOrg_id());

			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			Complaint complaint = null;

			if (l_ResultSet.next()) {
				complaint = new Complaint();

				// Mapping result set columns to Complaint object fields
				complaint.setComplaint_id(l_ResultSet.getString("complaint_id"));
				complaint.setOrg_id(l_ResultSet.getLong("org_id"));
				complaint.setOpr_id(l_ResultSet.getLong("opr_id"));
				complaint.setSubject(l_ResultSet.getString("subject"));
				complaint.setDescription(l_ResultSet.getString("description"));
				complaint.setPriority(l_ResultSet.getString("priority"));
				complaint.setStatus(l_ResultSet.getString("status"));
				complaint.setDepartment_id(l_ResultSet.getString("department_id"));
				complaint.setCreated_by(l_ResultSet.getString("created_by"));
				complaint.setAssigned_to(l_ResultSet.getString("assigned_to"));

				// Mapping Timestamp fields
				complaint.setCreated_on(l_ResultSet.getTimestamp("created_on"));
				complaint.setModified_on(l_ResultSet.getTimestamp("modified_on"));
				complaint.setL_department_name(l_ResultSet.getString("l_department_name"));
				complaint.setL_created_by(l_ResultSet.getString("l_created_by"));
				complaint.setL_assigned_to(l_ResultSet.getString("l_assigned_to"));
				complaint.setCategory_id(l_ResultSet.getString("category_id"));
				complaint.setL_category_name(l_ResultSet.getString("l_category_name"));
				complaint.setTag_id(l_ResultSet.getString("tag_id"));
				complaint.setLocation(l_ResultSet.getString("location"));

				String[] tagIdArray = complaint.getTag_id().split(",\\s*");
				if (tagIdArray != null || complaint.getTag_id().isEmpty()) {
					String tagId = "";
					for (int i = 0; i < tagIdArray.length; i++) {
						if (i > 0) {
							tagId += ",";
						}
						tagId += "'" + tagIdArray[i] + "'"; 
					}

					sql = "SELECT tag_name FROM tags_mst WHERE tag_id IN (" + tagId + ")";

					l_PreparedStatement = l_DBConnection.prepareStatement(sql);
					ResultSet l_ResultSet2 = l_PreparedStatement.executeQuery();
					StringBuilder tagNamesBuilder = new StringBuilder();
					while (l_ResultSet2.next()) {
					    if (tagNamesBuilder.length() > 0) {
					        tagNamesBuilder.append(",");
					    }
					    tagNamesBuilder.append(l_ResultSet2.getString("tag_name"));
					}

					String tagNamesCsv = tagNamesBuilder.toString();
					complaint.setL_tag_name(tagNamesCsv);

				}

				// Due date may be null, so check before mapping
				Timestamp dueDate = l_ResultSet.getTimestamp("due_date");
				complaint.setDue_date(dueDate != null ? dueDate : null);

				complaint.setModified_by(l_ResultSet.getString("modified_by"));
				complaint.setIs_active(l_ResultSet.getString("is_active"));
				complaint.setIs_anonymous(l_ResultSet.getString("is_anonymous"));
				complaint.setLocation(l_ResultSet.getString("location"));
			}

			if (complaint == null) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "Complaint not found");
			} else {
				sql = "SELECT COUNT(*) as count " + "FROM feedback_trn f "
						+ "WHERE f.is_active = 'YES' AND f.complaint_id = ?";

				l_PreparedStatement = l_DBConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				l_PreparedStatement.setString(1, complaint.getComplaint_id());
				ResultSet l_ResultSet1 = l_PreparedStatement.executeQuery();

				int count = 0;
				if (l_ResultSet1.next()) {
					count = l_ResultSet1.getInt("count");
				}
				complaint.setHas_feedback(count > 0);

				// get account_id from email
				sql = "SELECT account_id from account_user_mst WHERE email = ? ";

				l_PreparedStatement = l_DBConnection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				l_PreparedStatement.setString(1, request.getEmail());

				l_ResultSet = l_PreparedStatement.executeQuery();

				String accountId = null;

				if (l_ResultSet.next()) {
					accountId = l_ResultSet.getString("account_id");
					if (accountId == null || accountId.isBlank()) {
						return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND, "User not found");
					}
				}

				// anonymous logic
				if ("YES".equals(complaint.getIs_anonymous()) && !accountId.equals(complaint.getCreated_by())) {
					complaint.setCreated_by("");
					complaint.setL_created_by("");
				}

				return ResponseEntity.status(HttpStatus.OK).body(complaint);
			}
		} catch (Exception e) {
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

	}
	
	public String priorityBySentiment(Complaint complaint) {
		String priority = null;
		// if priority is blank or null then get priority using Sentiment Analysis
		if ("Very negative".equals(sentimentAnalysis.getSentiment(complaint.getDescription()))
				|| "Negative".equals(sentimentAnalysis.getSentiment(complaint.getDescription()))) {
			priority = "HIGH";
		} else if ("Neutral".equals(sentimentAnalysis.getSentiment(complaint.getDescription()))) {
			priority = "MEDIUM";
		} else if ("Positive".equals(sentimentAnalysis.getSentiment(complaint.getDescription()))
				|| "Very positive".equals(sentimentAnalysis.getSentiment(complaint.getDescription()))) {
			priority = "LOW";
		}
		return priority;
	}
	
	public Map<String, Object> getAllDepartmentWithMappedCategory(CommonRequestModel request) {
	    Map<String, Object> response = new HashMap<>();
	    Map<String, Map<String, Object>> labelMapping = new HashMap<>();

	    try (Connection conn = l_DataSource.getConnection()) {
	        StringBuilder query = new StringBuilder(
	            "SELECT d.department_id, d.department_name, c.category_id, c.category_name " +
	            "FROM departments_mst d " +
	            "LEFT JOIN category_mst c ON d.department_id = c.department_id " +
	            "WHERE d.is_active = 'YES' AND c.is_active = 'YES' " +
	            "AND d.org_id = ? AND d.opr_id = ?"
	        );

	        if (request.getId() != null && !request.getId().trim().isEmpty()) {
	            query.append(" AND d.department_id = ?");
	        }

	        PreparedStatement ps = conn.prepareStatement(query.toString());
	        ps.setLong(1, request.getOrg_id());
	        ps.setLong(2, request.getOpr_id());

	        if (request.getId() != null && !request.getId().trim().isEmpty()) {
	            ps.setString(3, request.getId());
	        }

	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	            String deptName = rs.getString("department_name");
	            String deptId = rs.getString("department_id");
	            String catId = rs.getString("category_id");
	            String catName = rs.getString("category_name");

	            if (!labelMapping.containsKey(deptName)) {
	                Map<String, Object> deptInfo = new HashMap<>();
	                deptInfo.put("id", deptId);
	                deptInfo.put("categories", new ArrayList<Map<String, Object>>());
	                labelMapping.put(deptName, deptInfo);
	            }

	            Map<String, Object> deptInfo = labelMapping.get(deptName);
	            List<Map<String, Object>> categories = (List<Map<String, Object>>) deptInfo.get("categories");

	            Map<String, Object> category = new HashMap<>();
	            category.put("id", catId);
	            category.put("name", catName);
	            categories.add(category);
	        }

	        response.put("label_mapping", labelMapping);
	        return response;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.put("error", "Database error occurred: " + e.getMessage());
	        return response;
	    }
	}
	

}
