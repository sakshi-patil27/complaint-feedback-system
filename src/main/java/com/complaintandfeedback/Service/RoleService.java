package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.Department;
import com.complaintandfeedback.Model.Role;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class RoleService {
	
	 @Autowired
	    private CommonUtils commonUtils;
		@Autowired
		private DataSource l_DataSource;
	 public ResponseEntity<Object> getAllRole(CommonRequestModel request) {
			Connection l_DBConnection = null;
			JSONArray l_ModuleArr = new JSONArray();

			try {
				l_DBConnection = l_DataSource.getConnection();

				String l_Query = "SELECT * FROM roles_mst WHERE is_active = 'YES' AND org_id = '"+request.getOrgId()+"' AND opr_id='"+request.getOprId()+"'"
						+"AND roles_name != 'Admin'";

				PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

				ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
				l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
				
				if (l_ModuleArr.isEmpty()) {
					return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
							"NO DATA FOUND");
				} else {
					TypeReference<List<Role>> typeReference = new TypeReference<List<Role>>() {
					};
					List<Role> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
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
