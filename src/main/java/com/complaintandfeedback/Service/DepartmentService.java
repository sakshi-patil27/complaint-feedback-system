package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Department;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DepartmentService {

//    @Autowired
//    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private CommonUtils commonUtils;
	@Autowired
	private DataSource l_DataSource;

    //private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResponseEntity<Object> saveDepartment(Department department) {
        Connection l_DBConnection = null;

        try {
            l_DBConnection = l_DataSource.getConnection();

            String departmentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            department.setDepartment_id(departmentId);

            // SQL Insert queryl_Query
            String l_Query = "INSERT INTO departments_mst (department_id, org_id, department_name, description, created_by, created_on, modified_by, modified_on, is_active, opr_id) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

            // Set the parameters for the insert query
            l_PreparedStatement.setString(1, department.getDepartment_id());
            l_PreparedStatement.setLong(2, department.getOrg_id());
            l_PreparedStatement.setString(3, department.getDepartment_name());
            l_PreparedStatement.setString(4, department.getDescription());
            l_PreparedStatement.setString(5, department.getCreated_by());
            l_PreparedStatement.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            l_PreparedStatement.setString(7, department.getCreated_by()); // Assuming createdBy is the same as modifiedBy on creation
            l_PreparedStatement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            l_PreparedStatement.setString(9, department.getIs_active());
            l_PreparedStatement.setLong(10, department.getOpr_id());

            int rowsAffected = l_PreparedStatement.executeUpdate();

            if (rowsAffected > 0) {
            	 return ResponseEntity.status(HttpStatus.CREATED).body(
                         new ResponseMessage("Success", "Department saved successfully", departmentId)
                     );
            } else {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save department");
            }
        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        } finally {
            if (l_DBConnection != null)
                try {
                    l_DBConnection.close();
                } catch (Exception e) {
                    return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
                }
        }
    }


    public ResponseEntity<Object> updateDepartment(Department department) {
        Connection l_DBConnection = null;

        try {
            l_DBConnection = l_DataSource.getConnection();
            String sql = "UPDATE departments_mst SET department_name = ?, description = ?, modified_by = ?, modified_on = ?, is_active = ?, opr_id = ? WHERE department_id = ?";
            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(sql);
   
            l_PreparedStatement.setString(1, department.getDepartment_name());
            l_PreparedStatement.setString(2, department.getDescription());
            l_PreparedStatement.setString(3, department.getModified_by());
            l_PreparedStatement.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            l_PreparedStatement.setString(5, department.getIs_active());
            l_PreparedStatement.setLong(6, department.getOpr_id());
            l_PreparedStatement.setString(7, department.getDepartment_id());
            int rowsAffected = l_PreparedStatement.executeUpdate();
            if (rowsAffected > 0) {
            	 return ResponseEntity.status(HttpStatus.CREATED).body(
            			 new ResponseMessage("Success", "Department Updated successfully", department.getDepartment_id()));
            } else {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to update department");
            }
        } catch (Exception e) {
            return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
        } finally {
            if (l_DBConnection != null)
                try {
                    l_DBConnection.close();
                } catch (Exception e) {
                    return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
                }
        }
    }


    public ResponseEntity<Object> getAllActiveDepartments(CommonRequestModel request) {
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();

		try {
			l_DBConnection = l_DataSource.getConnection();

			String l_Query = "SELECT * FROM departments_mst WHERE is_active = 'YES'AND org_id = ?"+" AND opr_id= ?";
			
			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			// Use parameter binding to avoid SQL injection
			l_PreparedStatement.setLong(1, request.getOrgId());
			l_PreparedStatement.setLong(2, request.getOprId());

			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			
			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"NO DATA FOUND");
			} else {
				TypeReference<List<Department>> typeReference = new TypeReference<List<Department>>() {
				};
				List<Department> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
						typeReference);
				return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
			}
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
		}

		finally {
			if (l_DBConnection != null)
				try {
					l_DBConnection.close();
				} catch (Exception e) {
					return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.UNAUTHORIZED, null);
				}
		}
	}

}
