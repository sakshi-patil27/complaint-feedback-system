package com.complaintandfeedback.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.Service.AttachmentService;


	@RestController
	@RequestMapping("/api/attachments")
	public class AttachmentTrnController {

	    @Autowired
	    private AttachmentService attachmentService;

	    @PostMapping("/saveAttachments")
	    public ResponseEntity<Object> saveAttachment(@RequestBody List<AttachmentTrn> attachmentTrn) {
	        return attachmentService.saveAttachments(attachmentTrn);
	    }
	    @PostMapping("/getAttachments")
	    public ResponseEntity<Object> getAttachments(@RequestBody CommonRequestModel request) {
	        return attachmentService.getAttachment(request);
	    }
	}

