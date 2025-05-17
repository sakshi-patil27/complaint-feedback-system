package com.complaintandfeedback.Model;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackQuestionResponse {
	
	private String response_id;
	
	private String feedback_id;
	
	private String question_id;
	
	private String selected_option_id;
	
	private String answer_text;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private String created_on;
	
	private String l_question_text;
	
	private String l_option_text;
}
