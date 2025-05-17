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

import com.complaintandfeedback.Model.FeedbackQuestion;
import com.complaintandfeedback.Model.FeedbackQuestionOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FeedbackQuestionService {
	
	@Autowired
	private CommonUtils commonUtils;

	@Autowired
	private DataSource l_DataSource;
			
	public ResponseEntity<Object> getFeedbackQuestion() {
		
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();
		
		try {
			// Get role of the user
			l_DBConnection = l_DataSource.getConnection();
			
			String l_Query = "SELECT * FROM feedback_questions_mst "
		               + "WHERE is_active = 'YES' "
		               + "ORDER BY created_on DESC";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE,
			        ResultSet.CONCUR_READ_ONLY);
	
			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
	
			if (l_ModuleArr.isEmpty()) {
			    return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
			} 
			else {
			    TypeReference<List<FeedbackQuestion>> typeReference = new TypeReference<List<FeedbackQuestion>>() {
			    };
			    List<FeedbackQuestion> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
			    
			    for(FeedbackQuestion feedbackQuestion : l_data_List) {
			    	
			    	l_Query = "SELECT * from feedback_question_options "
			    			+ "WHERE question_id = ? "
			 		        + "ORDER BY option_id DESC";
			    	
			    	l_PreparedStatement = l_DBConnection.prepareStatement(l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE,
					        ResultSet.CONCUR_READ_ONLY);
			    	
			    	l_PreparedStatement.setString(1,feedbackQuestion.getQuestion_id());
			    	
			    	l_ResultSet = l_PreparedStatement.executeQuery();
					l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			    	
					if (l_ModuleArr.isEmpty()) {
					    return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
					} 
					else {
					    TypeReference<List<FeedbackQuestionOptions>> typeReference1 = new TypeReference<List<FeedbackQuestionOptions>>() {
					    };
					    List<FeedbackQuestionOptions> l_data_List1 = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference1);
					    
					    feedbackQuestion.setFeedbackQuestionOptions(l_data_List1);
					}
			    }
			    
			    return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
			}
			
		}

		catch (Exception e) {
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
