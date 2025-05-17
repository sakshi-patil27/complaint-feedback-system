package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.Service.FeedbackQuestionService;


@RestController
@RequestMapping("/feedbackQuestion")
public class FeedbackQuestionController {
	
	@Autowired
	private FeedbackQuestionService feedbackQuestionService; 
	
	@GetMapping("/getFeedbackQuestion")
	public ResponseEntity<Object> getQuestion(){
		return feedbackQuestionService.getFeedbackQuestion();
	}
	
}
