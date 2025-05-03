package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.DTO.SuggestionDto;
import com.complaintandfeedback.Service.SuggestionService;

@RestController
@RequestMapping("/api/suggestion")
public class SuggestionController {
	
	@Autowired
	private SuggestionService suggestionService;
	
	@PostMapping("/saveSuggestion")
	public ResponseEntity<Object> saveFeedback(@RequestBody SuggestionDto suggestionDto){
		return suggestionService.saveSuggestion(suggestionDto);
	}
	
	@PostMapping("/getAllSuggestion")
	public ResponseEntity<Object> getAllSuggestion(@RequestBody CommonRequestModel request){
		return suggestionService.getAllSuggestion(request);
	}
	
	@PostMapping("/getSuggestionById")
	public ResponseEntity<Object> getSuggestionById(@RequestBody CommonRequestModel request){
		return suggestionService.getSuggestionById(request);
	}
	
}
