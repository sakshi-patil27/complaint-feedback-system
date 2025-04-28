package com.complaintandfeedback.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.Model.Department;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ResponseEntity<String> saveDepartment(Department department) {
        try {
            String departmentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            department.setDepartmentId(departmentId);

            String sql = "INSERT INTO departments_mst " +
                    "(department_id, org_id, department_name, description, created_by, created_on, modified_by, modified_on, is_active, opr_id) " +
                    "VALUES ('" + department.getDepartmentId() + "', " +
                    department.getOrgId() + ", '" +
                    department.getDepartmentName() + "', '" +
                    department.getDescription() + "', '" +
                    department.getCreatedBy() + "', CURRENT_TIMESTAMP, '" +
                    department.getModifiedBy() + "', CURRENT_TIMESTAMP, '" +
                    department.getIsActive() + "', " +
                    department.getOprId() + ")";

            jdbcTemplate.execute(sql);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Department saved successfully with ID: " + departmentId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving department: " + e.getMessage());
        }
    }

    public ResponseEntity<List<Department>> getAllActiveDepartments() {
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
