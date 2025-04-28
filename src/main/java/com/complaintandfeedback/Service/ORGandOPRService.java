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

import com.complaintandfeedback.Model.Opr;
import com.complaintandfeedback.Model.Org;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ORGandOPRService {

	@Autowired
	private CommonUtils commonUtils;
	
	@Autowired
	private DataSource l_DataSource;
	
	public ResponseEntity<Object> getAllActiveORGS() {
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();
		
		try {
			l_DBConnection = l_DataSource.getConnection();
			String l_Query = "SELECT * FROM org WHERE is_active = 'YES'";
			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			
			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"NO DATA FOUND");
			}else {
				TypeReference<List<Org>> typeReference = new TypeReference<List<Org>>() {
				};
				List<Org> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
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

	public ResponseEntity<Object> getAllActiveOPRS() {
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();
		
		try {
			l_DBConnection = l_DataSource.getConnection();
			String l_Query = "SELECT * FROM opr WHERE is_active = 'YES'";
			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			
			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			
			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"NO DATA FOUND");
			}else {
				TypeReference<List<Opr>> typeReference = new TypeReference<List<Opr>>() {
				};
				List<Opr> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
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
