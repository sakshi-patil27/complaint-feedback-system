package com.complaintandfeedback.Controller;

import java.sql.SQLException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.DTO.ComplaintDto;
import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Service.ComplaintService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {
	
	@Autowired
	private ComplaintService complaintService; 
	
    @PostMapping("/saveComplaint")
    public ResponseEntity<Object> saveComplaint(@RequestBody ComplaintDto complaint) throws SQLException {
        return complaintService.saveComplaint(complaint);
    }
    @PostMapping("/updateComplaint")
    public ResponseEntity<Object> updateComplaint(@Valid @RequestBody Complaint complaint) throws SQLException {
        return complaintService.updateComplaint(complaint);
    }
	
    @PostMapping("/updateStatus")
    public ResponseEntity<Object> updateStatus(@Valid @RequestBody Complaint complaint) throws SQLException {
    	return complaintService.updateStatus(complaint);
    }
    @PostMapping("/getAllComplaint")
    public ResponseEntity<Object> getAllActiveComplaint(@RequestBody CommonRequestModel request) {
        return complaintService.getAllActiveComplaints(request);
    }    
    
    @PostMapping("/getComplaintById")
    public ResponseEntity<Object> getComplaintById(@RequestBody CommonRequestModel request) {
        return complaintService.getComplaintById(request);
    } 
    
    @PostMapping("/getAllDepartmentWithMappedCategory")
    public Map<String, Object> getAllDepartmentWithMappedCategory(@RequestBody CommonRequestModel request) {
        return  complaintService.getAllDepartmentWithMappedCategory(request);
    } 
    
}
