package com.complaintandfeedback.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentTrn {
    private String attachment_id;
    private String entity_type;
    private String entity_id;
    private String file_path;
    private String stored_file_name;
    private String Uploaded_file_name;
    private String uploaded_by;
    private String uploaded_on;
    private String l_encrypted_file;
} 