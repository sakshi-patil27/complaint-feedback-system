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
import com.complaintandfeedback.Model.Category_Mst;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CategoryService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private DataSource l_DataSource;

    public ResponseEntity<Object> saveCategory(Category_Mst category) {
        Connection l_DBConnection = null;

        try {
            l_DBConnection = l_DataSource.getConnection();

            String categoryId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            category.setCategory_id(categoryId);

            String l_Query = "INSERT INTO category_mst (category_id, category_name, description, department_id, org_id, opr_id, created_on, is_active) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

            l_PreparedStatement.setString(1, category.getCategory_id());
            l_PreparedStatement.setString(2, category.getCategory_name());
            l_PreparedStatement.setString(3, category.getDescription());
            l_PreparedStatement.setString(4, category.getDepartment_id());
            l_PreparedStatement.setLong(5, category.getOrg_id());
            l_PreparedStatement.setObject(6, category.getOpr_id()); // may be null
            l_PreparedStatement.setString(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            l_PreparedStatement.setString(8, category.getIs_active());

            int rowsAffected = l_PreparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new ResponseMessage("Success", "Category saved successfully", categoryId)
                );
            } else {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save category");
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

    public ResponseEntity<Object> getAllCategories(CommonRequestModel request) {
        Connection l_DBConnection = null;
        JSONArray l_ModuleArr = new JSONArray();

        try {
            l_DBConnection = l_DataSource.getConnection();

            String l_Query = "SELECT * FROM category_mst WHERE is_active = 'YES'";
            l_Query=l_Query+" AND org_id = ? AND opr_id = ?";

            if (request.getId() != null && !request.getId().isEmpty()) {
            	l_Query=l_Query+" AND department_id = ?";
            }

            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            l_PreparedStatement.setLong(1, request.getOrg_id());
            l_PreparedStatement.setLong(2, request.getOpr_id());

            if (request.getId() != null && !request.getId().isEmpty()) {
                l_PreparedStatement.setString(3, request.getId());
            }

            ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
            l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

            if (l_ModuleArr.isEmpty()) {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
            } else {
                TypeReference<List<Category_Mst>> typeReference = new TypeReference<List<Category_Mst>>() {};
                List<Category_Mst> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
                return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
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
}
