package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public class DashboardService {
	

	@Autowired
	private DataSource l_DataSource;

	@Autowired
    private CommonUtils commonUtils;


	public ResponseEntity<Object> getComplaintSummaryByStatusForAdmin(CommonRequestModel request) {
	    Connection l_DBConnection = null;
	    try {
	        l_DBConnection = l_DataSource.getConnection();

	        String query = "SELECT status, COUNT(*) as count " +
	                       "FROM complaint_trn " +
	                       "WHERE is_active = 'YES' " +
	                       "AND opr_id = ? AND org_id = ? " +
	                       "GROUP BY status";

	        PreparedStatement stmt = l_DBConnection.prepareStatement(query);
	        stmt.setLong(1, request.getOprId());
	        stmt.setLong(2, request.getOrgId());

	        ResultSet rs = stmt.executeQuery();

	        List<Map<String, Object>> resultList = new ArrayList<>();

	        while (rs.next()) {
	            Map<String, Object> obj = new HashMap<>();
	            obj.put("status", rs.getString("status"));
	            obj.put("count", rs.getInt("count"));
	            resultList.add(obj);
	        }

	        return ResponseEntity.ok(resultList);

	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	    } finally {
	        if (l_DBConnection != null) {
	            try {
	                l_DBConnection.close();
	            } catch (Exception e) {
	                return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	            }
	        }
	    }
	}
	public ResponseEntity<Object> getComplaintSummaryByDepartment(CommonRequestModel request) {
	    Connection l_DBConnection = null;
	    try {
	        l_DBConnection = l_DataSource.getConnection();

	        String query = "SELECT dm.department_name, ct.status, COUNT(*) AS count " +
	                       "FROM complaint_trn ct " +
	                       "JOIN departments_mst dm ON ct.department_id = dm.department_id " +
	                       "WHERE ct.is_active = 'YES' " +
	                       "AND ct.opr_id = ? " +
	                       "AND ct.org_id = ? " +
	                       "GROUP BY dm.department_name, ct.status";

	        PreparedStatement stmt = l_DBConnection.prepareStatement(query);
	        stmt.setLong(1, request.getOprId());
	        stmt.setLong(2, request.getOrgId());

	        ResultSet rs = stmt.executeQuery();

	        Map<String, Map<String, Integer>> departmentStatusMap = new HashMap<>();

	        while (rs.next()) {
	            String departmentName = rs.getString("department_name");
	            String status = rs.getString("status");
	            int count = rs.getInt("count");

	            if (!departmentStatusMap.containsKey(departmentName)) {
	                departmentStatusMap.put(departmentName, new HashMap<>());
	            }

	            departmentStatusMap.get(departmentName).put(status, count);
	        }

	        List<Map<String, Object>> resultList = new ArrayList<>();

	        for (Map.Entry<String, Map<String, Integer>> entry : departmentStatusMap.entrySet()) {
	            Map<String, Object> departmentData = new HashMap<>();
	            departmentData.put("department_name", entry.getKey());
	            departmentData.put("status_counts", entry.getValue());

	            resultList.add(departmentData);
	        }

	        return ResponseEntity.ok(resultList);

	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	    } finally {
	        if (l_DBConnection != null) {
	            try {
	                l_DBConnection.close();
	            } catch (Exception e) {
	                return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	            }
	        }
	    }
	}


}

