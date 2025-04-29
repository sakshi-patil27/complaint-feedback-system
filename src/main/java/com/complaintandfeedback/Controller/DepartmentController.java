package com.complaintandfeedback.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Department;
import com.complaintandfeedback.Service.DepartmentService;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;
    
    // Save new Department
    @PostMapping("/saveDepartment")
    public ResponseEntity<Object> saveDepartment(@RequestBody Department department) {
        return departmentService.saveDepartment(department);
    }

    // Update existing Department
    @PutMapping("/updateDepartment")
    public ResponseEntity<Object> updateDepartment(@RequestBody Department department) {
        return departmentService.updateDepartment(department);
    }

    // Get all active Departments
    @PostMapping("/getAllDepartment")
    public ResponseEntity<Object> getAllActiveDepartments(@RequestBody CommonRequestModel request) {
        return departmentService.getAllActiveDepartments(request);
    }
}
