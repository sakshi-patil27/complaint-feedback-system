package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Service.ComplaintStatusHistoryService;

@RestController
@RequestMapping("/api/complaintStatusHistory")
public class ComplaintStatusHistoryController {
	
	@Autowired
	private ComplaintStatusHistoryService complaintStatusHistoryService;
	
	//Get Complaint History by complaint id
	@PostMapping("/getHistoryByComplaint")
	public ResponseEntity<Object> getHistoryByComplaint(@RequestBody CommonRequestModel request){
		return complaintStatusHistoryService.getHistoryByComplaint(request);
	}
}
