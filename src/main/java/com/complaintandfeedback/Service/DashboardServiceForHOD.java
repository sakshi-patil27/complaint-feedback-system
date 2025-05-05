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
public class DashboardServiceForHOD {

    @Autowired
    private DataSource l_DataSource;

    @Autowired
    private CommonUtils commonUtils;

    // 1. Complaint Summary by Status (HOD-specific)
    public ResponseEntity<Object> getComplaintSummaryByStatusForHOD(CommonRequestModel request, Long departmentId) {
        try (Connection l_DBConnection = l_DataSource.getConnection()) {

            String query = "SELECT status, COUNT(*) AS count " +
                           "FROM complaint_trn " +
                           "WHERE is_active = 'YES' " +
                           "AND opr_id = ? AND org_id = ? AND department_id = ? " +
                           "GROUP BY status";

            try (PreparedStatement stmt = l_DBConnection.prepareStatement(query)) {
                stmt.setLong(1, request.getOpr_id());
                stmt.setLong(2, request.getOrg_id());
                stmt.setLong(3, departmentId);

                try (ResultSet rs = stmt.executeQuery()) {
                    List<Map<String, Object>> resultList = new ArrayList<>();
                    int totalComplaints = 0;

                    while (rs.next()) {
                        Map<String, Object> obj = new HashMap<>();
                        String status = rs.getString("status");
                        int count = rs.getInt("count");
                        obj.put("status", status);
                        obj.put("count", count);
                        resultList.add(obj);
                        totalComplaints += count;
                    }

                    String resolutionTimeQuery = "SELECT AVG(resolution_time_hours) AS avg_resolution_time_hours FROM (" +
                            " SELECT csh.complaint_id, TIMESTAMPDIFF(HOUR, " +
                            "   MIN(CASE WHEN csh.to_status = 'Open' THEN csh.changed_on END), " +
                            "   MAX(CASE WHEN csh.to_status = 'Closed' THEN csh.changed_on END) " +
                            " ) AS resolution_time_hours " +
                            " FROM complaint_status_history csh " +
                            " JOIN complaint_trn ct ON csh.complaint_id = ct.complaint_id " +
                            " WHERE ct.status = 'Closed' AND ct.is_active = 'YES' " +
                            " AND ct.opr_id = ? AND ct.org_id = ? AND ct.department_id = ? " +
                            " GROUP BY csh.complaint_id" +
                            ") AS sub";

                    try (PreparedStatement resolutionStmt = l_DBConnection.prepareStatement(resolutionTimeQuery)) {
                        resolutionStmt.setLong(1, request.getOpr_id());
                        resolutionStmt.setLong(2, request.getOrg_id());
                        resolutionStmt.setLong(3, departmentId);

                        try (ResultSet resolutionRs = resolutionStmt.executeQuery()) {
                            double avgResolutionTime = 0.0;
                            if (resolutionRs.next()) {
                                avgResolutionTime = resolutionRs.getDouble("avg_resolution_time_hours");
                            }

                            Map<String, Object> response = new HashMap<>();
                            response.put("totalComplaints", totalComplaints);
                            response.put("statusSummary", resultList);
                            response.put("avgResolutionTime", avgResolutionTime);

                            return ResponseEntity.ok(response);
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

    // 2. Complaint Summary by Status (Single Department Only)
    public ResponseEntity<Object> getComplaintSummaryByDepartmentForHOD(CommonRequestModel request, Long departmentId) {
        try (Connection l_DBConnection = l_DataSource.getConnection()) {

            String query = "SELECT dm.department_name, ct.status, COUNT(*) AS count " +
                           "FROM complaint_trn ct " +
                           "JOIN departments_mst dm ON ct.department_id = dm.department_id " +
                           "WHERE ct.is_active = 'YES' AND ct.opr_id = ? AND ct.org_id = ? AND ct.department_id = ? " +
                           "GROUP BY dm.department_name, ct.status";

            try (PreparedStatement stmt = l_DBConnection.prepareStatement(query)) {
                stmt.setLong(1, request.getOpr_id());
                stmt.setLong(2, request.getOrg_id());
                stmt.setLong(3, departmentId);

                ResultSet rs = stmt.executeQuery();

                Map<String, Map<String, Integer>> departmentStatusMap = new HashMap<>();

                while (rs.next()) {
                    String departmentName = rs.getString("department_name");
                    String status = rs.getString("status");
                    int count = rs.getInt("count");

                    departmentStatusMap.computeIfAbsent(departmentName, k -> new HashMap<>()).put(status, count);
                }

                List<Map<String, Object>> resultList = new ArrayList<>();

                for (Map.Entry<String, Map<String, Integer>> entry : departmentStatusMap.entrySet()) {
                    Map<String, Object> departmentData = new HashMap<>();
                    departmentData.put("department_name", entry.getKey());
                    departmentData.put("status_counts", entry.getValue());
                    resultList.add(departmentData);
                }

                return ResponseEntity.ok(resultList);
            }
        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    // 3. Complaints By Month for HOD
    public ResponseEntity<Object> getComplaintsByMonthsForHOD(CommonRequestModel request, Long departmentId) {
        try (Connection l_DBConnection = l_DataSource.getConnection()) {

            String sql = "SELECT " +
                         "DATE_FORMAT(created_on, '%m') AS month, " +
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
                         "WHERE is_active = 'YES' AND org_id = ? AND opr_id = ? AND department_id = ? " +
                         "GROUP BY DATE_FORMAT(created_on, '%m') " +
                         "ORDER BY month DESC";

            try (PreparedStatement stmt = l_DBConnection.prepareStatement(sql)) {
                stmt.setLong(1, request.getOrg_id());
                stmt.setLong(2, request.getOpr_id());
                stmt.setLong(3, departmentId);

                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> result = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> row = Map.of(
                            "month", rs.getString("month"),
                            "total_complaints", rs.getInt("total_complaints"),
                            "open_complaints", rs.getInt("open_complaints"),
                            "assigned_complaints", rs.getInt("assigned_complaints"),
                            "in_progress_complaints", rs.getInt("in_progress_complaints"),
                            "resolved_complaints", rs.getInt("resolved_complaints"),
                            "reopened_complaints", rs.getInt("reopened_complaints"),
                            "escalated_complaints", rs.getInt("escalated_complaints"),
                            "deferred_complaints", rs.getInt("deferred_complaints"),
                            "closed_complaints", rs.getInt("closed_complaints")
                    );
                    result.add(row);
                }

                return ResponseEntity.ok(result);
            }

        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }
}
