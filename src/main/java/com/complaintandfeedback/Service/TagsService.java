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
import com.complaintandfeedback.Model.ResponseMessage;
import com.complaintandfeedback.Model.Tags;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TagsService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private DataSource l_DataSource;

    public ResponseEntity<Object> saveTags(Tags tags) {
        Connection l_DBConnection = null;

        try {
            l_DBConnection = l_DataSource.getConnection();

            String tagId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            tags.setTag_id(tagId);

            String l_Query = "INSERT INTO tags_mst (tag_id, tag_name, category_id, org_id, opr_id, is_active, created_by, created_on, modified_by,modified_on ) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

            l_PreparedStatement.setString(1, tags.getTag_id());
            l_PreparedStatement.setString(2, tags.getTag_name());
            l_PreparedStatement.setString(3, tags.getCategory_id());
            l_PreparedStatement.setLong(4, tags.getOrg_id());
            l_PreparedStatement.setLong(5, tags.getOpr_id());
            l_PreparedStatement.setString(6, tags.getIs_active());
            l_PreparedStatement.setString(7, tags.getCreated_by());
            l_PreparedStatement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            l_PreparedStatement.setString(9, tags.getModified_by());
            l_PreparedStatement.setString(10, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int rowsAffected = l_PreparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                        new ResponseMessage("Success", "Tag saved successfully", tagId)
                );
            } else {
                return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Failed to save tag");
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

    public ResponseEntity<Object> getTagsByCategory(CommonRequestModel request) {
        Connection l_DBConnection = null;
        JSONArray l_ModuleArr = new JSONArray();

        try {
            l_DBConnection = l_DataSource.getConnection();

            String l_Query = "SELECT * FROM tags_mst WHERE is_active = 'YES' AND org_id = ? AND opr_id = ? ";
            if (request.getId() != null && !request.getId().isEmpty()) {
            	l_Query=l_Query+" AND category_id = ?";
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
                TypeReference<List<Tags>> typeReference = new TypeReference<List<Tags>>() {};
                List<Tags> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
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
