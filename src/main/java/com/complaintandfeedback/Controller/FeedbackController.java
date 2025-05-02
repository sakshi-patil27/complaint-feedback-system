package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Feedback;
import com.complaintandfeedback.Service.FeedbackService;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
	
	@Autowired
	private FeedbackService feedbackService;
	
	@PostMapping("/saveFeedback")
	public ResponseEntity<Object> saveFeedback(@RequestBody Feedback feedback){
		return feedbackService.saveFeedback(feedback);
	}
	
	@PostMapping("/getFeedbackByComplaint")
	public ResponseEntity<Object> getFeedbackByComplaint(@RequestBody CommonRequestModel request){
		return feedbackService.getFeedbackByComplaint(request);
	}
	
}
