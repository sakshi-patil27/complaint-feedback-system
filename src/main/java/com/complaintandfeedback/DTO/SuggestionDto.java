package com.complaintandfeedback.DTO;

import java.util.ArrayList;
import java.util.List;

import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.Model.Suggestion;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionDto {
	
	@Valid
	private Suggestion suggestion;
	
	@Valid
	private List<AttachmentTrn> attachments = new ArrayList<>();
	
}
