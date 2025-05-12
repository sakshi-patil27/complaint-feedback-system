package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Tags;
import com.complaintandfeedback.Service.TagsService;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

	@Autowired
	private TagsService tagsService;
	
	@PostMapping("/saveTags")
	public ResponseEntity<Object> saveTags(@RequestBody Tags tags){
		return tagsService.saveTags(tags);
	}
	
	@PostMapping("/getTagsByCategory")
	public ResponseEntity<Object> getTagsByCategory(@RequestBody CommonRequestModel request){
		return tagsService.getTagsByCategory(request);
	}
}
