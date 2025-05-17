package com.complaintandfeedback.DTO;

import java.util.ArrayList;
import java.util.List;

import com.complaintandfeedback.Model.Feedback;
import com.complaintandfeedback.Model.FeedbackQuestionResponse;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDto {
	
	@Valid
	private Feedback feedback;
	
	private List<FeedbackQuestionResponse> feedbackQuestionResponse = new ArrayList<>();
	
}
