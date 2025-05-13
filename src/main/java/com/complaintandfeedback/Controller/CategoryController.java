package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Category_Mst;
import com.complaintandfeedback.Service.CategoryService;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;
	
	@PostMapping("/saveCategory")
	public ResponseEntity<Object> saveCategory(@RequestBody Category_Mst category_Mst){
		return categoryService.saveCategory(category_Mst);
	}
	
	@PostMapping("/getCategoryByDepartment")
	public ResponseEntity<Object> getCategoryByDepartment(@RequestBody CommonRequestModel request){
		return categoryService.getAllCategories(request);
	}
}
