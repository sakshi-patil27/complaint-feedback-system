package com.complaintandfeedback.Controller;

import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


	@RestController
	@RequestMapping("/api/attachments")
	public class AttachmentTrnController {

	    @Autowired
	    private AttachmentService attachmentService;

	    @PostMapping("/saveAttachments")
	    public ResponseEntity<Object> saveAttachment(@RequestBody AttachmentTrn attachmentTrn) {
	        return attachmentService.saveAttachment(attachmentTrn);
	    }
	    @PostMapping("/getAttachments")
	    public ResponseEntity<Object> getAttachments(@RequestBody CommonRequestModel request) {
	        return attachmentService.getAttachment(request);
	    }
	}

