package com.complaintandfeedback.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.Department;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResponseEntity<Object> saveDepartment(Department department) {
        try {
            String departmentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            department.setDepartmentId(departmentId);

            String sql = "INSERT INTO departments_mst (department_id, org_id, department_name, description, created_by, created_on, modified_by, modified_on, is_active, opr_id) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    departmentId,
                    department.getOrgId(),
                    department.getDepartmentName(),
                    department.getDescription(),
                    department.getCreatedBy(),
                    LocalDateTime.now().format(formatter),
                    department.getCreatedBy(), // On create, modifiedBy = createdBy
                    LocalDateTime.now().format(formatter),
                    department.getIsActive(),
                    department.getOprId()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Department saved successfully with ID: " + departmentId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving department: " + e.getMessage());
        }
    }

    public ResponseEntity<Object> updateDepartment(Department department) {
        try {
            String sql = "UPDATE departments_mst SET department_name = ?, description = ?, modified_by = ?, modified_on = ?, is_active = ?, opr_id = ? WHERE department_id = ?";

            int updatedRows = jdbcTemplate.update(sql,
                    department.getDepartmentName(),
                    department.getDescription(),
                    department.getModifiedBy(),
                    LocalDateTime.now().format(formatter),
                    department.getIsActive(),
                    department.getOprId(),
                    department.getDepartmentId()
            );

            if (updatedRows > 0) {
                return ResponseEntity.ok("Department updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Department not found with ID: " + department.getDepartmentId());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating department: " + e.getMessage());
        }
    }

    public ResponseEntity<Object> getAllActiveDepartments() {
        try {
            String sql = "SELECT * FROM departments_mst WHERE is_active = 'YES'";
            List<Department> departments = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Department.class));

            if (departments.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); 
            }

            return ResponseEntity.ok(departments);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
