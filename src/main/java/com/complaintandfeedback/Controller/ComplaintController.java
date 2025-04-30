package com.complaintandfeedback.Controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Complaint;
import com.complaintandfeedback.Service.ComplaintService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {
	
	@Autowired
	private ComplaintService complaintService; 
	
	// Save new Complaint
    @PostMapping("/saveComplaint")
    public ResponseEntity<Object> saveComplaint(@Valid @RequestBody Complaint complaint) throws SQLException {
        return complaintService.saveComplaint(complaint);
    }
	
 // Get all active Complaint
    @PostMapping("/getAllComplaint")
    public ResponseEntity<Object> getAllActiveComplaint(@RequestBody CommonRequestModel request) {
        return complaintService.getAllActiveDepartments(request);
    }
    
}
