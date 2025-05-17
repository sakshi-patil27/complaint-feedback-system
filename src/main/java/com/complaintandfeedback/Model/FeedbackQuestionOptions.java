package com.complaintandfeedback.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackQuestionOptions {
	
	private String option_id;
	
	private String question_id;
	
	private String option_text;
	
	private String option_order;
	
}
