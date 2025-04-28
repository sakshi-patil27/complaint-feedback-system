package com.complaintandfeedback.Controller;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Department;
import com.complaintandfeedback.Service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
