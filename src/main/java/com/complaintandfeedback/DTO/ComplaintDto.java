package com.complaintandfeedback.DTO;

import java.util.ArrayList;
import java.util.List;

import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.Model.Complaint;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintDto {
	
	@Valid
	private Complaint complaint;
	
	@Valid
	private List<AttachmentTrn> attachments = new ArrayList<>();
	
}
