package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Service.DashboardUserService;
@RestController
@RequestMapping("/api/Dashboard")
public class DashboardUserController {
	@Autowired
	private DashboardUserService dashboardUserService;
	@PostMapping("/user/complaint-status-summary")
	public ResponseEntity<Object> getComplaintStatusSummary(@RequestBody CommonRequestModel request) {
	    return dashboardUserService.getComplaintCountByUser(request);
	}
	@PostMapping("/user/by-months")
    public ResponseEntity<Object> getComplaintsByMonths(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaintsByMonths(request);
    }
}
