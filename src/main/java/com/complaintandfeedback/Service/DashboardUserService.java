package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Complaint;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DashboardUserService {

    private final PasswordEncoder passwordEncoder;
	@Autowired
	private CommonUtils commonUtils;
	@Autowired
	private DataSource l_DataSource;

    DashboardUserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

	public ResponseEntity<Object> getComplaintSummaryByUser(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {

			// 1. Get count grouped by status
			String statusCountQuery = "SELECT status, COUNT(*) AS count " + "FROM complaint_trn "
					+ "WHERE is_active = 'YES' " + "AND created_by = ? " + "AND opr_id = ? " + "AND org_id = ? "
					+ "GROUP BY status";

			try (PreparedStatement stmt = l_DBConnection.prepareStatement(statusCountQuery)) {
				stmt.setString(1, request.getId()); // User ID
				stmt.setLong(2, request.getOpr_id());
				stmt.setLong(3, request.getOrg_id());

				List<Map<String, Object>> statusSummary = new ArrayList<>();
				int totalComplaints = 0;

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						Map<String, Object> obj = new HashMap<>();
						String status = rs.getString("status");
						int count = rs.getInt("count");
						obj.put("status", status);
						obj.put("count", count);
						statusSummary.add(obj);
						totalComplaints += count;
					}
				}

				// 2. Calculate average resolution time
				String resolutionTimeQuery = "SELECT AVG(resolution_time_hours) AS avg_resolution_time_hours FROM ("
						+ " SELECT csh.complaint_id, TIMESTAMPDIFF(HOUR, "
						+ "   MIN(CASE WHEN csh.to_status = 'OPEN' THEN csh.changed_on END), "
						+ "   MAX(CASE WHEN csh.to_status = 'CLOSED' THEN csh.changed_on END)) "
						+ " AS resolution_time_hours " + " FROM complaint_status_history csh "
						+ " JOIN complaint_trn ct ON csh.complaint_id = ct.complaint_id "
						+ " WHERE ct.status = 'CLOSED' AND ct.is_active = 'YES' "
						+ " AND ct.created_by = ? AND ct.opr_id = ? AND ct.org_id = ? "
						+ " GROUP BY csh.complaint_id ) AS sub";

				double avgResolutionTime = 0.0;
				try (PreparedStatement resolutionStmt = l_DBConnection.prepareStatement(resolutionTimeQuery)) {
					resolutionStmt.setString(1, request.getId());
					resolutionStmt.setLong(2, request.getOpr_id());
					resolutionStmt.setLong(3, request.getOrg_id());

					try (ResultSet rs = resolutionStmt.executeQuery()) {
						if (rs.next()) {
							avgResolutionTime = rs.getDouble("avg_resolution_time_hours");
						}
					}
				}

				// 3. Calculate average feedback rating
				String ratingQuery = "SELECT AVG(f.rating) AS avg_rating " + "FROM feedback_trn f "
						+ "JOIN complaint_trn ct ON f.complaint_id = ct.complaint_id "
						+ "WHERE ct.is_active = 'YES' AND ct.created_by = ? AND ct.opr_id = ? AND ct.org_id = ?";

				double avgRating = 0.0;
				try (PreparedStatement ratingStmt = l_DBConnection.prepareStatement(ratingQuery)) {
					ratingStmt.setString(1, request.getId());
					ratingStmt.setLong(2, request.getOpr_id());
					ratingStmt.setLong(3, request.getOrg_id());

					try (ResultSet rs = ratingStmt.executeQuery()) {
						if (rs.next()) {
							avgRating = rs.getDouble("avg_rating");
						}
					}
				}

				// 4. Build final response
				Map<String, Object> response = new HashMap<>();
				response.put("totalComplaints", totalComplaints);
				response.put("statusSummary", statusSummary);
				response.put("avgResolutionTime", avgResolutionTime);
				response.put("avgRating", avgRating);

				return ResponseEntity.ok(response);

			}
		} catch (SQLException e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR,
					"Database error occurred.");
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR,
					"Unexpected error occurred.");
		}
	}

	public ResponseEntity<Object> getComplaintsByMonths(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {
			StringBuilder sql = new StringBuilder("SELECT DATE_FORMAT(created_on, '%m') AS month, "
					+ "COUNT(*) AS total_complaints, "
					+ "SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS open_complaints, "
					+ "SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) AS assigned_complaints, "
					+ "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_complaints, "
					+ "SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_complaints, "
					+ "SUM(CASE WHEN status = 'REOPEN' THEN 1 ELSE 0 END) AS reopened_complaints, "
					+ "SUM(CASE WHEN status = 'ESCALATED' THEN 1 ELSE 0 END) AS escalated_complaints, "
					+ "SUM(CASE WHEN status = 'DEFERRED' THEN 1 ELSE 0 END) AS deferred_complaints, "
					+ "SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) AS closed_complaints " + "FROM complaint_trn "
					+ "WHERE is_active = 'YES' " + "AND org_id = ? AND opr_id = ? " + "AND created_by = ?");

			sql.append("GROUP BY DATE_FORMAT(created_on, '%m') ORDER BY month DESC");

			PreparedStatement stmt = l_DBConnection.prepareStatement(sql.toString());
			stmt.setLong(1, request.getOrg_id());
			stmt.setLong(2, request.getOpr_id());
			stmt.setString(3, request.getId());

			ResultSet rs = stmt.executeQuery();
			List<Map<String, Object>> result = new ArrayList<>();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("month", rs.getString("month"));
				row.put("total_complaints", rs.getInt("total_complaints"));
				row.put("open_complaints", rs.getInt("open_complaints"));
				row.put("assigned_complaints", rs.getInt("assigned_complaints"));
				row.put("in_progress_complaints", rs.getInt("in_progress_complaints"));
				row.put("resolved_complaints", rs.getInt("resolved_complaints"));
				row.put("reopened_complaints", rs.getInt("reopened_complaints"));
				row.put("escalated_complaints", rs.getInt("escalated_complaints"));
				row.put("deferred_complaints", rs.getInt("deferred_complaints"));
				row.put("closed_complaints", rs.getInt("closed_complaints"));
				result.add(row);
			}

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getComplaintSummaryByAssignee(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {

			// 1. Query to get complaint count grouped by status
			String statusCountQuery = "SELECT status, COUNT(*) AS count " + "FROM complaint_trn "
					+ "WHERE is_active = 'YES' AND assigned_to = ? " + "AND opr_id = ? AND org_id = ? "
					+ "GROUP BY status";

			List<Map<String, Object>> statusSummary = new ArrayList<>();
			int totalComplaints = 0;

			try (PreparedStatement stmt = l_DBConnection.prepareStatement(statusCountQuery)) {
				stmt.setString(1, request.getId()); // assigned_to
				stmt.setLong(2, request.getOpr_id());
				stmt.setLong(3, request.getOrg_id());

				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String status = rs.getString("status");
						int count = rs.getInt("count");

						Map<String, Object> statusMap = new HashMap<>();
						statusMap.put("status", status);
						statusMap.put("count", count);

						statusSummary.add(statusMap);
						totalComplaints += count;
					}
				}
			}

			// 2. Query to calculate average resolution time
			String resolutionTimeQuery = "SELECT AVG(resolution_time_hours) AS avg_resolution_time_hours FROM ("
					+ " SELECT csh.complaint_id, TIMESTAMPDIFF(HOUR, "
					+ "   MIN(CASE WHEN csh.to_status = 'OPEN' THEN csh.changed_on END), "
					+ "   MAX(CASE WHEN csh.to_status = 'CLOSED' THEN csh.changed_on END)) "
					+ " AS resolution_time_hours " + " FROM complaint_status_history csh "
					+ " JOIN complaint_trn ct ON csh.complaint_id = ct.complaint_id "
					+ " WHERE ct.status = 'CLOSED' AND ct.is_active = 'YES' "
					+ " AND ct.assigned_to = ? AND ct.opr_id = ? AND ct.org_id = ? "
					+ " GROUP BY csh.complaint_id ) AS sub " + " WHERE resolution_time_hours IS NOT NULL";

			double avgResolutionTime = 0.0;
			try (PreparedStatement stmt = l_DBConnection.prepareStatement(resolutionTimeQuery)) {
				stmt.setString(1, request.getId());
				stmt.setLong(2, request.getOpr_id());
				stmt.setLong(3, request.getOrg_id());

				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						avgResolutionTime = rs.getDouble("avg_resolution_time_hours");
					}
				}
			}

			// 3. Query to calculate average rating
			String ratingQuery = "SELECT AVG(f.rating) AS avg_rating " + "FROM feedback_trn f "
					+ "JOIN complaint_trn ct ON f.complaint_id = ct.complaint_id "
					+ "WHERE ct.is_active = 'YES' AND ct.assigned_to = ? " + "AND ct.opr_id = ? AND ct.org_id = ?";

			double avgRating = 0.0;
			try (PreparedStatement stmt = l_DBConnection.prepareStatement(ratingQuery)) {
				stmt.setString(1, request.getId());
				stmt.setLong(2, request.getOpr_id());
				stmt.setLong(3, request.getOrg_id());

				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						avgRating = rs.getDouble("avg_rating");
					}
				}
			}

			// 4. Final Response
			Map<String, Object> response = new HashMap<>();
			response.put("totalComplaints", totalComplaints);
			response.put("statusSummary", statusSummary);
			response.put("avgResolutionTime", avgResolutionTime);
			response.put("avgRating", avgRating);

			return ResponseEntity.ok(response);

		} catch (SQLException e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR,
					"Database error occurred.");
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR,
					"Unexpected error occurred.");
		}
	}

	public ResponseEntity<Object> getComplaintsByMonthsByAssign(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {
			StringBuilder sql = new StringBuilder("SELECT DATE_FORMAT(created_on, '%m') AS month, "
					+ "COUNT(*) AS total_complaints, "
					+ "SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS open_complaints, "
					+ "SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) AS assigned_complaints, "
					+ "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_complaints, "
					+ "SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_complaints, "
					+ "SUM(CASE WHEN status = 'REOPEN' THEN 1 ELSE 0 END) AS reopened_complaints, "
					+ "SUM(CASE WHEN status = 'ESCALATED' THEN 1 ELSE 0 END) AS escalated_complaints, "
					+ "SUM(CASE WHEN status = 'DEFERRED' THEN 1 ELSE 0 END) AS deferred_complaints, "
					+ "SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) AS closed_complaints " + "FROM complaint_trn "
					+ "WHERE is_active = 'YES' " + "AND org_id = ? AND opr_id = ? " + "AND assigned_to = ?");

			sql.append("GROUP BY DATE_FORMAT(created_on, '%m') ORDER BY month DESC");

			PreparedStatement stmt = l_DBConnection.prepareStatement(sql.toString());
			stmt.setLong(1, request.getOrg_id());
			stmt.setLong(2, request.getOpr_id());
			stmt.setString(3, request.getId());

			ResultSet rs = stmt.executeQuery();
			List<Map<String, Object>> result = new ArrayList<>();

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();
				row.put("month", rs.getString("month"));
				row.put("total_complaints", rs.getInt("total_complaints"));
				row.put("open_complaints", rs.getInt("open_complaints"));
				row.put("assigned_complaints", rs.getInt("assigned_complaints"));
				row.put("in_progress_complaints", rs.getInt("in_progress_complaints"));
				row.put("resolved_complaints", rs.getInt("resolved_complaints"));
				row.put("reopened_complaints", rs.getInt("reopened_complaints"));
				row.put("escalated_complaints", rs.getInt("escalated_complaints"));
				row.put("deferred_complaints", rs.getInt("deferred_complaints"));
				row.put("closed_complaints", rs.getInt("closed_complaints"));
				result.add(row);
			}

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getComplaintsByPriority(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {
			String l_Query = "SELECT count(*) count ,priority " 
		            + "FROM complaint_trn " 
					+ "WHERE is_active = 'YES' "
					+ "AND org_id = ? AND opr_id = ? " 
					+ "AND created_by = ? " 
					+ "GROUP BY priority ";
			PreparedStatement l_PreparedStatement= l_DBConnection.prepareStatement(l_Query);
			l_PreparedStatement.setLong(1, request.getOrg_id());
			l_PreparedStatement.setLong(2, request.getOpr_id());
			l_PreparedStatement.setString(3, request.getId());
			ResultSet rs=l_PreparedStatement.executeQuery();
			List<Map<String, Object>> priorityList = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> map=new HashMap<String, Object>();
			     map.put("priority", rs.getString("priority"));
                 map.put("count", rs.getInt("count"));
				priorityList.add(map);
			}
			return ResponseEntity.ok(priorityList);
		}catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}

	public ResponseEntity<Object> getComplaintsByPriorityByAssign(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {
			String l_Query = "SELECT count(*) count ,priority " 
		            + "FROM complaint_trn " 
					+ "WHERE is_active = 'YES' "
					+ "AND org_id = ? AND opr_id = ? " 
					+ "AND assigned_to = ? " 
					+ "GROUP BY priority ";
			PreparedStatement l_PreparedStatement= l_DBConnection.prepareStatement(l_Query);
			l_PreparedStatement.setLong(1, request.getOrg_id());
			l_PreparedStatement.setLong(2, request.getOpr_id());
			l_PreparedStatement.setString(3, request.getId());
			ResultSet rs=l_PreparedStatement.executeQuery();
			List<Map<String, Object>> priorityList = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> map=new HashMap<String, Object>();
			     map.put("priority", rs.getString("priority"));
                 map.put("count", rs.getInt("count"));
				priorityList.add(map);
			}
			return ResponseEntity.ok(priorityList);
		}catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}
	public ResponseEntity<Object> getComplaints(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {
			String l_Query = "SELECT * " 
		            + "FROM complaint_trn " 
					+ "WHERE is_active = 'YES' "
					+ "AND org_id = ? AND opr_id = ? " 
					+ "AND created_by = ? "
					+ "ORDER BY created_on";
			PreparedStatement l_PreparedStatement= l_DBConnection.prepareStatement(l_Query);
			l_PreparedStatement.setLong(1, request.getOrg_id());
			l_PreparedStatement.setLong(2, request.getOpr_id());
			l_PreparedStatement.setString(3, request.getId());
			ResultSet l_ResultSet=l_PreparedStatement.executeQuery();
			JSONArray l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
			} else {
				TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
				};
				List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
				return ResponseEntity.ok(l_data_List);
			}
		}catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}
	public ResponseEntity<Object> getComplaintsByAssign(CommonRequestModel request) {
		try (Connection l_DBConnection = l_DataSource.getConnection()) {
			String l_Query = "SELECT * " 
		            + "FROM complaint_trn " 
					+ "WHERE is_active = 'YES' "
					+ "AND org_id = ? AND opr_id = ? " 
					+ "AND assigned_to = ? "
					+ "ORDER BY created_on ";
			PreparedStatement l_PreparedStatement= l_DBConnection.prepareStatement(l_Query);
			l_PreparedStatement.setLong(1, request.getOrg_id());
			l_PreparedStatement.setLong(2, request.getOpr_id());
			l_PreparedStatement.setString(3, request.getId());
			ResultSet l_ResultSet=l_PreparedStatement.executeQuery();
			JSONArray l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
			} else {
				TypeReference<List<Complaint>> typeReference = new TypeReference<List<Complaint>>() {
				};
				List<Complaint> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
				return ResponseEntity.ok(l_data_List);
			}
		}catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
		}
	}



}
