package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.Service.ORGandOPRService;

@RestController
@RequestMapping("/api/organdopr")
public class ORGandOPRController {
	
	@Autowired
	private ORGandOPRService oRGandOPRService; 
	
	// Get all active ORGS
    @GetMapping("/getAllOrgs")
    public ResponseEntity<Object> getAllOrgs() {
        return oRGandOPRService.getAllActiveORGS();
    }
    
    // Get all active OPRS
    @GetMapping("/getAllOprs")
    public ResponseEntity<Object> getAllOprs() {
        return oRGandOPRService.getAllActiveOPRS();
    }
	
}
