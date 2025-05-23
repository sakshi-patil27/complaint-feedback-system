package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.CommonRequestModel;
@Service
public class DashboardAdminHODService {
	

	@Autowired
	private DataSource l_DataSource;

	@Autowired
    private CommonUtils commonUtils;

	public ResponseEntity<Object> getComplaintSummaryByStatusForAdmin(CommonRequestModel request) {
	    try (Connection l_DBConnection = l_DataSource.getConnection()) {

	        // 1. Prepare query to fetch complaint count by status
	        StringBuilder query = new StringBuilder(
	            "SELECT status, COUNT(*) AS count " +
	            "FROM complaint_trn " +
	            "WHERE is_active = 'YES' " +
	            "AND opr_id = ? AND org_id = ? "
	        );

	        if (request.getId() != null) {
	            query.append("AND department_id = ? ");
	        }

	        query.append("GROUP BY status");

	        try (PreparedStatement stmt = l_DBConnection.prepareStatement(query.toString())) {
	            stmt.setLong(1, request.getOpr_id());
	            stmt.setLong(2, request.getOrg_id());
	            if (request.getId() != null) {
	                stmt.setString(3, request.getId());
	            }

	            try (ResultSet rs = stmt.executeQuery()) {
	                List<Map<String, Object>> resultList = new ArrayList<>();
	                int totalComplaints = 0;

	                // 2. Collect complaint count grouped by status
	                while (rs.next()) {
	                    Map<String, Object> obj = new HashMap<>();
	                    String status = rs.getString("status");
	                    int count = rs.getInt("count");
	                    obj.put("status", status);
	                    obj.put("count", count);
	                    resultList.add(obj);
	                    totalComplaints += count;
	                }

	                // 3. Query to calculate average resolution time (in hours)
	                String resolutionTimeQuery =
	                    "SELECT AVG(resolution_time_hours) AS avg_resolution_time_hours FROM (" +
	                    " SELECT csh.complaint_id, TIMESTAMPDIFF(HOUR, " +
	                    "   MIN(CASE WHEN csh.to_status = 'OPEN' THEN csh.changed_on END), " +
	                    "   MAX(CASE WHEN csh.to_status = 'CLOSED' THEN csh.changed_on END)) " +
	                    " AS resolution_time_hours " +
	                    " FROM complaint_status_history csh " +
	                    " JOIN complaint_trn ct ON csh.complaint_id = ct.complaint_id " +
	                    " WHERE ct.status = 'CLOSED' AND ct.is_active = 'YES' " +
	                    " AND ct.opr_id = ? AND ct.org_id = ? ";

	                if (request.getId() != null) {
	                    resolutionTimeQuery += " AND ct.department_id = ? ";
	                }

	                resolutionTimeQuery += " GROUP BY csh.complaint_id ) AS sub";

	                try (PreparedStatement resolutionStmt = l_DBConnection.prepareStatement(resolutionTimeQuery)) {
	                    resolutionStmt.setLong(1, request.getOpr_id());
	                    resolutionStmt.setLong(2, request.getOrg_id());
	                    if (request.getId() != null) {
	                        resolutionStmt.setNString(3, request.getId());
	                    }

	                    try (ResultSet resolutionRs = resolutionStmt.executeQuery()) {
	                        double avgResolutionTime = 0.0;
	                        if (resolutionRs.next()) {
	                            avgResolutionTime = resolutionRs.getDouble("avg_resolution_time_hours");
	                        }

	                        // 4. Query to calculate average rating from feedback table
	                        String ratingQuery =
	                            "SELECT AVG(f.rating) AS avg_rating " +
	                            "FROM feedback_trn f " +
	                            "JOIN complaint_trn ct ON f.complaint_id = ct.complaint_id " +
	                            "WHERE ct.is_active = 'YES' AND ct.opr_id = ? AND ct.org_id = ?";

	                        if (request.getId() != null) {
	                            ratingQuery += " AND ct.department_id = ? ";
	                        }

	                        try (PreparedStatement ratingStmt = l_DBConnection.prepareStatement(ratingQuery)) {
	                            ratingStmt.setLong(1, request.getOpr_id());
	                            ratingStmt.setLong(2, request.getOrg_id());
	                            if (request.getId() != null) {
	                                ratingStmt.setNString(3, request.getId());
	                            }

	                            try (ResultSet ratingRs = ratingStmt.executeQuery()) {
	                                double avgRating = 0.0;
	                                if (ratingRs.next()) {
	                                    avgRating = ratingRs.getDouble("avg_rating");
	                                }

	                                // 5. Build final response map
	                                Map<String, Object> response = new HashMap<>();
	                                response.put("totalComplaints", totalComplaints);
	                                response.put("statusSummary", resultList);
	                                response.put("avgResolutionTime", avgResolutionTime);
	                                response.put("avgRating", avgRating);

	                                return ResponseEntity.ok(response);
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    } catch (SQLException e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurred.");
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
	    }
	}


	public ResponseEntity<Object> getComplaintSummaryByDepartment(CommonRequestModel request) {try (Connection l_DBConnection = l_DataSource.getConnection()) {
	    StringBuilder query = new StringBuilder(
	            "SELECT dm.department_name, ct.status, COUNT(*) AS count " +
	            "FROM complaint_trn ct " +
	            "JOIN departments_mst dm ON ct.department_id = dm.department_id " +
	            "WHERE ct.is_active = 'YES' " +
	            "AND ct.opr_id = ? AND ct.org_id = ? "
	        );

	        if (request.getId() != null) {
	            query.append("AND ct.department_id = ? ");
	        }

	        query.append("GROUP BY dm.department_name, ct.status");

	        PreparedStatement stmt = l_DBConnection.prepareStatement(query.toString());
	        stmt.setLong(1, request.getOpr_id());
	        stmt.setLong(2, request.getOrg_id());
	        if (request.getId() != null) {
	            stmt.setString(3, request.getId());
	        }

	        ResultSet rs = stmt.executeQuery();
	        Map<String, Map<String, Integer>> departmentStatusMap = new HashMap<>();
	        Map<String, Integer> departmentTotalCount = new HashMap<>();

	        while (rs.next()) {
	            String departmentName = rs.getString("department_name");
	            String status = rs.getString("status");
	            int count = rs.getInt("count");

	            // Add status count
	            departmentStatusMap
	                .computeIfAbsent(departmentName, k -> new HashMap<>())
	                .put(status, count);

	            // Add to total count
	            departmentTotalCount.put(
	                departmentName,
	                departmentTotalCount.getOrDefault(departmentName, 0) + count
	            );
	        }

	        List<Map<String, Object>> resultList = new ArrayList<>();
	        for (Map.Entry<String, Map<String, Integer>> entry : departmentStatusMap.entrySet()) {
	            String departmentName = entry.getKey();
	            Map<String, Integer> statusCounts = entry.getValue();
	            int total = departmentTotalCount.getOrDefault(departmentName, 0);

	            Map<String, Object> departmentData = new HashMap<>();
	            departmentData.put("department_name", departmentName);
	            departmentData.put("status_counts", statusCounts);
	            departmentData.put("total", total);  
	            resultList.add(departmentData);
	        }

	        return ResponseEntity.ok(resultList);
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
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
	            "AND org_id = ? AND opr_id = ? "
	        );

	        if (request.getId() != null) {
	            sql.append("AND department_id = ? ");
	        }

	        sql.append("GROUP BY DATE_FORMAT(created_on, '%m') ORDER BY month DESC");

	        PreparedStatement stmt = l_DBConnection.prepareStatement(sql.toString());
	        stmt.setLong(1, request.getOrg_id());
	        stmt.setLong(2, request.getOpr_id());
	        if (request.getId() != null) {
	            stmt.setString(3, request.getId());
	        }

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


	/**
	 * Get complaint count grouped by priority (Low, Medium, High).
	 */
	public ResponseEntity<Object> getComplaintsByPriority(CommonRequestModel request) {
	    try (Connection connection = l_DataSource.getConnection()) {

	        StringBuilder query = new StringBuilder(
	            "SELECT priority, COUNT(*) AS count " +
	            "FROM complaint_trn " +
	            "WHERE is_active = 'YES' AND opr_id = ? AND org_id = ? "
	        );
	        if (request.getId() != null) {
	            query.append("AND department_id = ? ");
	        }
	        query.append("GROUP BY priority");

	        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
	            stmt.setLong(1, request.getOpr_id());
	            stmt.setLong(2, request.getOrg_id());
	            if (request.getId() != null) {
	                stmt.setString(3, request.getId());
	            }

	            List<Map<String, Object>> priorityList = new ArrayList<>();
	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    Map<String, Object> map = new HashMap<>();
	                    map.put("priority", rs.getString("priority"));
	                    map.put("count", rs.getInt("count"));
	                    priorityList.add(map);
	                }
	            }

	            return ResponseEntity.ok(priorityList);
	        }
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching complaints by priority.");
	    }
	}

	/**
	 * Get top 5 pending complaints nearing their due date (within next 3 days).
	 */
	public ResponseEntity<Object> getPendingComplaintsNearingDueDate(CommonRequestModel request) {
	    try (Connection connection = l_DataSource.getConnection()) {

	        StringBuilder query = new StringBuilder(
	            "SELECT complaint_id, subject, due_date, department_id, priority " +
	            "FROM complaint_trn " +
	            "WHERE is_active = 'YES' "+
	            " AND status NOT IN ('CLOSED', 'ESCALATED') "+
	            "AND due_date BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL 2 DAY) " +
	            "AND opr_id = ? AND org_id = ? "
	        );
	        if (request.getId() != null) {
	            query.append("AND department_id = ? ");
	        }
	        query.append("ORDER BY due_date ASC LIMIT 5");

	        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
	            stmt.setLong(1, request.getOpr_id());
	            stmt.setLong(2, request.getOrg_id());
	            if (request.getId() != null) {
	                stmt.setString(3, request.getId());
	            }

	            List<Map<String, Object>> complaintsList = new ArrayList<>();
	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    Map<String, Object> map = new HashMap<>();
	                    map.put("complaintId", rs.getString("complaint_id"));
	                    map.put("subject", rs.getString("subject"));
	                    map.put("dueDate", rs.getDate("due_date"));
	                    map.put("departmentId", rs.getString("department_id"));
	                    map.put("priority", rs.getString("priority"));
	                    complaintsList.add(map);
	                }
	            }

	            return ResponseEntity.ok(complaintsList);
	        }
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching nearing due date complaints.");
	    }
	}
	public ResponseEntity<Object> getUserLoadByDepartment(CommonRequestModel request) {
	    try (Connection connection = l_DataSource.getConnection()) {
	        String query = """
	            SELECT 
	                a.name,
	                c.assigned_to,
	                COUNT(*) AS current_load,
	                ROUND(
	                    COUNT(*) * 100.0 / (
	                        SELECT COUNT(*) 
	                        FROM complaint_trn 
	                        WHERE department_id = ? 
	                          AND org_id = ? 
	                          AND opr_id = ? 
	                          AND status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'Reopen', 'Escalated')
	                    ), 
	                    2
	                ) AS load_percentage
	            FROM complaint_trn c
	            JOIN account_user_mst a ON c.assigned_to = a.account_id
	            WHERE c.department_id = ?
	              AND c.org_id = ?
	              AND c.opr_id = ?
	              AND c.status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'Reopen', 'Escalated')
	            GROUP BY c.assigned_to, a.name
	            ORDER BY load_percentage ASC
	        """;

	        try (PreparedStatement stmt = connection.prepareStatement(query)) {
	            stmt.setString(1, request.getId());           
	            stmt.setLong(2, request.getOrg_id());        
	            stmt.setLong(3, request.getOpr_id());        

	            stmt.setString(4, request.getId());           
	            stmt.setLong(5, request.getOrg_id());         
	            stmt.setLong(6, request.getOpr_id());         

	            List<Map<String, Object>> teamLoadList = new ArrayList<>();

	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    Map<String, Object> map = new HashMap<>();
	                    map.put("fullName", rs.getString("name"));
	                    map.put("account_id", rs.getString("assigned_to"));
	                    map.put("currentLoad", rs.getInt("current_load"));
	                    map.put("loadPercentage", rs.getDouble("load_percentage"));
	                    teamLoadList.add(map);
	                }
	            }

	            return ResponseEntity.ok(teamLoadList);
	        }
	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(
	            e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, "Error while fetching team load with percentage.");
	    }
	}
	
	public ResponseEntity<Object> getComplaintSummaryByCategoryAndTags(CommonRequestModel request) {
	    try (Connection conn = l_DataSource.getConnection()) {

	        // 1. Get complaints count grouped by department and subject (tag)
	        String sql = "SELECT d.department_id, d.department_name, " +
	                     "       ct.subject AS tag_name, COUNT(*) AS tag_count " +
	                     "FROM complaint_trn ct " +
	                     "JOIN departments_mst d ON ct.department_id = d.department_id " +
	                     "WHERE ct.is_active = 'YES' AND ct.opr_id = ? AND ct.org_id = ? ";

	        if (request.getId() != null) {
	            sql += "AND ct.department_id = ? ";
	        }

	        sql += "GROUP BY d.department_id, ct.subject";

	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setLong(1, request.getOpr_id());
	        stmt.setLong(2, request.getOrg_id());
	        if (request.getId() != null) {
	            stmt.setString(3, request.getId());
	        }

	        ResultSet rs = stmt.executeQuery();

	        // 2. Process results into nested map
	        Map<String, Map<String, Object>> departmentMap = new LinkedHashMap<>();

	        while (rs.next()) {
	            String deptId = rs.getString("department_id");
	            String deptName = rs.getString("department_name");
	            String tagName = rs.getString("tag_name");
	            int tagCount = rs.getInt("tag_count");

	            // Create new department if not exists
	            departmentMap.putIfAbsent(deptId, new HashMap<>() {{
	                put("category_id", deptId);
	                put("category_name", deptName);
	                put("count", 0);
	                put("tags", new ArrayList<Map<String, Object>>());
	            }});

	            Map<String, Object> dept = departmentMap.get(deptId);
	            List<Map<String, Object>> tags = (List<Map<String, Object>>) dept.get("tags");

	            // Add tag
	            Map<String, Object> tagObj = new HashMap<>();
	            tagObj.put("tag_id", UUID.randomUUID().toString());
	            tagObj.put("tag_name", tagName);
	            tagObj.put("count", tagCount);
	            tags.add(tagObj);

	            // Update total count
	            int currentCount = (int) dept.get("count");
	            dept.put("count", currentCount + tagCount);
	        }

	        // 3. Final result list
	        List<Map<String, Object>> resultList = new ArrayList<>(departmentMap.values());
	        return ResponseEntity.ok(resultList);

	    } catch (SQLException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "SQL error", "details", e.getMessage()));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "Unexpected error", "details", e.getMessage()));
	    }
	}


	}


