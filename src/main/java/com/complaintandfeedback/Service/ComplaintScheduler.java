package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.complaintandfeedback.Model.Complaint;

@Component
public class ComplaintScheduler {
		
	@Autowired
	private ComplaintService complaintService;
	
	@Autowired
	private DataSource l_DataSource;
	
	// Scheduled task that runs every minute
    @Scheduled(cron = "0 0 * * * ?")  // Cron expression for every hour
    public void checkComplaintsAndEscalate() throws Exception {
        System.out.println("Scheduled Task - Checking complaints for escalation: " + LocalDateTime.now());

        Connection l_DBConnection = null;
        PreparedStatement l_PreparedStatement = null;
        ResultSet l_ResultSet = null;

        try {
            l_DBConnection = l_DataSource.getConnection();

            // Get all active complaints
            String sql = "SELECT * FROM complaint_trn WHERE is_active = 'YES'";

            l_PreparedStatement = l_DBConnection.prepareStatement(
                    sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            l_ResultSet = l_PreparedStatement.executeQuery();

            List<Complaint> complaints = new ArrayList<>();

            // Check if ResultSet has rows
            while (l_ResultSet.next()) {
                Complaint complaint = new Complaint();

                // Mapping ResultSet columns to the Complaint object
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
                complaint.setCreated_on(l_ResultSet.getTimestamp("created_on"));
                complaint.setModified_on(l_ResultSet.getTimestamp("modified_on"));
                complaint.setModified_by(l_ResultSet.getString("modified_by"));
                complaint.setIs_active(l_ResultSet.getString("is_active"));

                // Handle Timestamp for due_date
                Timestamp dueDate = l_ResultSet.getTimestamp("due_date");
                complaint.setDue_date(dueDate != null ? dueDate : null);

                complaints.add(complaint);
            }

            // Process complaints for escalation
            Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

            for (Complaint complaint : complaints) {
                if (complaint.getDue_date() != null && complaint.getDue_date().before(currentTimestamp) && !"ESCALATED".equals(complaint.getStatus())
                		&& !"CLOSED".equals(complaint.getStatus())) {
                    // Due date has passed, so escalate the status
                    complaint.setStatus("ESCALATED");

                    // Update the complaint status
                    ResponseEntity<Object> response = complaintService.updateStatus(complaint);
                    if (response.getStatusCode().equals(HttpStatus.OK)) {
                        System.out.println("Complaint " + complaint.getComplaint_id() + " status escalated.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during scheduled task: " + e.getMessage());
        } finally {
            // Clean up database resources
            if (l_ResultSet != null) {
                try {
                    l_ResultSet.close();
                } catch (Exception e) {
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
            if (l_PreparedStatement != null) {
                try {
                    l_PreparedStatement.close();
                } catch (Exception e) {
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (l_DBConnection != null) {
                try {
                    l_DBConnection.close();
                } catch (Exception e) {
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
            }
        }
    }
	
}
