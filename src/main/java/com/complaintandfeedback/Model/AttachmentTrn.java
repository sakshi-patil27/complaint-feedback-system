package com.complaintandfeedback.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentTrn {
    private String attachment_id;
    private String entity_type;
    private String entity_id;
    private String file_path;
    private String Stored_file_name;
    private String Uploaded_file_name;
    private String uploaded_by;
    private LocalDateTime uploaded_on = LocalDateTime.now();
    private String l_encrypted_file;
} 