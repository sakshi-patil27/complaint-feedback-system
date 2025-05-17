package com.complaintandfeedback.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackQuestion {
	
	private String question_id;
	
	@Column(columnDefinition = "TEXT")
	private String question_text;
		
	private String question_type;
	
	private String is_active;
	
	private String created_on;
	
	private List<FeedbackQuestionOptions> feedbackQuestionOptions = new ArrayList<>();
	
}
