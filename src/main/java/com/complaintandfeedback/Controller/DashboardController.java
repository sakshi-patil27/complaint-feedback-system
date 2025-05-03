package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Service.DashboardService;
@RestController
@RequestMapping("/api/Dashboard")
public class DashboardController {
	@Autowired
	private DashboardService dashboardService;
	
	@PostMapping("/admin/complaint-status-summary")
	public ResponseEntity<Object> getComplaintStatusSummary(@RequestBody CommonRequestModel request) {
	    return dashboardService.getComplaintSummaryByStatusForAdmin(request);
	}
	
	@PostMapping("/admin/by-department-status")
    public ResponseEntity<Object> getComplaintSummaryByDepartmentAndStatus(@RequestBody CommonRequestModel request) {
        return dashboardService.getComplaintSummaryByDepartment(request);
    }

}
