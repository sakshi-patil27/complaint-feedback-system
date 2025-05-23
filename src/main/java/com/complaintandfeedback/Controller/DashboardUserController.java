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
	    return dashboardUserService.getComplaintSummaryByUser(request);
	}
	@PostMapping("/user/by-months")
    public ResponseEntity<Object> getComplaintsByMonths(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaintsByMonths(request);
    }
	@PostMapping("/user/assign/complaint-status-summary")
	public ResponseEntity<Object> getComplaintStatusSummaryByAssign(@RequestBody CommonRequestModel request) {
	    return dashboardUserService.getComplaintSummaryByAssignee(request);
	}
	@PostMapping("/user/assign/by-months")
    public ResponseEntity<Object> getComplaintsByMonthsByAssign(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaintsByMonthsByAssign(request);
    }
	@PostMapping("/user/by-priority")
    public ResponseEntity<Object> getComplaintsByPriority(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaintsByPriority(request);
    }
	@PostMapping("/user/assign/by-priority")
    public ResponseEntity<Object> getComplaintsByPriorityByAssign(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaintsByPriorityByAssign(request);
    }
	@PostMapping("/user/complaintList")
    public ResponseEntity<Object> getComplaints(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaints(request);
    }
	@PostMapping("/user/assign/complaintList")
    public ResponseEntity<Object> getComplaintsByAssign(@RequestBody CommonRequestModel request) {
        return dashboardUserService.getComplaintsByAssign(request);
    }
}
