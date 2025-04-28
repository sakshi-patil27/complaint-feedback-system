package com.complaintandfeedback.Model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Org {
	
	private Long org_id;	
	private String org_name;
	private String created_by;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp created_on;
	private String	modified_by;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Timestamp modified_on;
	private String is_active;
}
