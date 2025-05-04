package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.CommonRequestModel;
@Service
public class DashboardUserService {
	@Autowired
	private CommonUtils commonUtils;
	@Autowired
	private DataSource l_DataSource;
	public ResponseEntity<Object> getComplaintCountByUser(CommonRequestModel request) {
	    try (Connection l_DBConnection = l_DataSource.getConnection()) {

	        String statusCountQuery = "SELECT status, COUNT(*) AS complaint_count " +
	                                  "FROM complaint_trn " +
	                                  "WHERE is_active = 'YES' " +
	                                  "AND created_by = ? " +
	                                  "AND opr_id = ? " +
	                                  "AND org_id = ? " +
	                                  "GROUP BY status";

	        String resolutionTimeQuery = 
	        	    "SELECT AVG(resolution_time_hours) AS avg_resolution_time_hours FROM (" +
	        	    "  SELECT csh.complaint_id, TIMESTAMPDIFF(HOUR, " +
	        	    "    MIN(CASE WHEN csh.to_status = 'OPEN' THEN csh.changed_on END), " +
	        	    "    MAX(CASE WHEN csh.to_status = 'CLOSED' THEN csh.changed_on END)) AS resolution_time_hours " +
	        	    "  FROM complaint_status_history csh " +
	        	    "  LEFT JOIN complaint_trn ct ON csh.complaint_id = ct.complaint_id " +
	        	    "  WHERE ct.status = 'CLOSED' AND ct.is_active = 'YES' " +
	        	    "   AND ct.created_by = ? "
	        	    + " AND ct.opr_id = ? AND ct.org_id = ? " +
	        	    "  GROUP BY csh.complaint_id" +
	        	    ") AS resolution_data";


	        Map<String, Object> response = new HashMap<>();
	        int total = 0;
	        Map<String, Integer> statusCounts = new HashMap<>();

	        // Get count per status
	        try (PreparedStatement stmt = l_DBConnection.prepareStatement(statusCountQuery)) {
	            stmt.setString(1, request.getId());
	            stmt.setLong(2, request.getOprId());
	            stmt.setLong(3, request.getOrgId());

	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    String status = rs.getString("status");
	                    int count = rs.getInt("complaint_count");
	                    statusCounts.put(status, count);
	                    total += count;
	                }
	            }
	        }

	        Long avgResolutionHours = null;
	        try (PreparedStatement stmt = l_DBConnection.prepareStatement(resolutionTimeQuery)) {
	            stmt.setString(1, request.getId());
	            stmt.setLong(2, request.getOprId());
	            stmt.setLong(3, request.getOrgId());

	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    avgResolutionHours = rs.getLong("avg_resolution_time_hours"); // Fixed name
	                    if (rs.wasNull()) {
	                        avgResolutionHours = null;
	                    }
	                }
	            }
	        }

	        response.put("totalComplaintCount", total);
	        response.put("statusWiseCount", statusCounts);
	        response.put("avgResolutionTimeInSeconds", avgResolutionHours);

	        return ResponseEntity.ok(response);

	    } catch (SQLException e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred.");
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
	    }
	}
	public ResponseEntity<Object> getComplaintsByMonths(CommonRequestModel request) {
	    try (Connection l_DBConnection = l_DataSource.getConnection()) {
	        StringBuilder sql = new StringBuilder(
	            "SELECT DATE_FORMAT(created_on, '%m') AS month, " +
	            "COUNT(*) AS total_complaints, " +
	            "SUM(CASE WHEN status = 'OPEN' THEN 1 ELSE 0 END) AS open_complaints, " +
	            "SUM(CASE WHEN status = 'ASSIGNED' THEN 1 ELSE 0 END) AS assigned_complaints, " +
	            "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_complaints, " +
	            "SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_complaints, " +
	            "SUM(CASE WHEN status = 'REOPEN' THEN 1 ELSE 0 END) AS reopened_complaints, " +
	            "SUM(CASE WHEN status = 'ESCALATED' THEN 1 ELSE 0 END) AS escalated_complaints, " +
	            "SUM(CASE WHEN status = 'DEFERRED' THEN 1 ELSE 0 END) AS deferred_complaints, " +
	            "SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) AS closed_complaints " +
	            "FROM complaint_trn " +
	            "WHERE is_active = 'YES' " +
	            "AND org_id = ? AND opr_id = ? "+
	            "AND created_by = ?"
	        );

	        sql.append("GROUP BY DATE_FORMAT(created_on, '%m') ORDER BY month DESC");

	        PreparedStatement stmt = l_DBConnection.prepareStatement(sql.toString());
	        stmt.setLong(1, request.getOrgId());
	        stmt.setLong(2, request.getOprId());
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
	public ResponseEntity<Object> getComplaintCountByAssignee(CommonRequestModel request) {
	    try (Connection l_DBConnection = l_DataSource.getConnection()) {

	        String statusCountQuery = "SELECT status, COUNT(*) AS complaint_count " +
	                                  "FROM complaint_trn " +
	                                  "WHERE is_active = 'YES' " +
	                                  "AND assigned_to = ? " +  // <-- Changed
	                                  "AND opr_id = ? " +
	                                  "AND org_id = ? " +
	                                  "GROUP BY status";

	        String resolutionTimeQuery = 
	            "SELECT AVG(resolution_time_hours) AS avg_resolution_time_hours FROM (" +
	            "  SELECT csh.complaint_id, TIMESTAMPDIFF(HOUR, " +
	            "    MIN(CASE WHEN csh.to_status = 'OPEN' THEN csh.changed_on END), " +
	            "    MAX(CASE WHEN csh.to_status = 'CLOSED' THEN csh.changed_on END)) AS resolution_time_hours " +
	            "  FROM complaint_status_history csh " +
	            "  JOIN complaint_trn ct ON csh.complaint_id = ct.complaint_id " +
	            "  WHERE ct.status = 'CLOSED' AND ct.is_active = 'YES' " +
	            "   AND ct.assigned_to = ? " +  // <-- Changed
	            "   AND ct.opr_id = ? AND ct.org_id = ? " +
	            "  GROUP BY csh.complaint_id" +
	            ") AS resolution_data WHERE resolution_time_hours IS NOT NULL";


	        Map<String, Object> response = new HashMap<>();
	        int total = 0;
	        Map<String, Integer> statusCounts = new HashMap<>();

	        // Status count
	        try (PreparedStatement stmt = l_DBConnection.prepareStatement(statusCountQuery)) {
	            stmt.setString(1, request.getId());
	            stmt.setLong(2, request.getOprId());
	            stmt.setLong(3, request.getOrgId());

	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    String status = rs.getString("status");
	                    int count = rs.getInt("complaint_count");
	                    statusCounts.put(status, count);
	                    total += count;
	                }
	            }
	        }

	        // Avg resolution time
	        Long avgResolutionHours = null;
	        try (PreparedStatement stmt = l_DBConnection.prepareStatement(resolutionTimeQuery)) {
	            stmt.setString(1, request.getId());
	            stmt.setLong(2, request.getOprId());
	            stmt.setLong(3, request.getOrgId());

	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    avgResolutionHours = rs.getLong("avg_resolution_time_hours");
	                    if (rs.wasNull()) {
	                        avgResolutionHours = null;
	                    }
	                }
	            }
	        }

	        response.put("totalComplaintCount", total);
	        response.put("statusWiseCount", statusCounts);
	        response.put("avgResolutionTimeInHours", avgResolutionHours);

	        return ResponseEntity.ok(response);

	    } catch (SQLException e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred.");
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
	    }
	}


}
