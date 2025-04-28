package com.complaintandfeedback.Controller;
import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Department;
import com.complaintandfeedback.Service.DepartmentService;
import com.complaintandfeedback.Service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/roles")
public class RoleController {
	@Autowired
	private RoleService roleService;

	@PostMapping(path="/getAllRole",consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getAllActiveRoles(@RequestBody CommonRequestModel request) {
		return roleService.getAllRole(request);
	}
}
