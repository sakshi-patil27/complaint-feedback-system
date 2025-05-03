package com.complaintandfeedback.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.AttachmentTrn;
import com.complaintandfeedback.Model.Department;
import com.complaintandfeedback.Model.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AttachmentService {
	@Autowired
	private CommonUtils commonUtils;
	@Autowired
	private DataSource l_DataSource;

	public ResponseEntity<Object> saveAttachments(List<AttachmentTrn> attachmentList) {
	    Connection l_DBConnection = null;
	    List<ResponseMessage> responseMessages = new ArrayList<>();

	    try {
	        l_DBConnection = l_DataSource.getConnection();

	        String l_File_Path = commonUtils.gFN_Uploaded_File_Path();
	        String l_Query = "INSERT INTO attachment_trn (attachment_id, entity_type, entity_id, "
	                + "file_path, Stored_file_name, Uploaded_file_name, uploaded_by, uploaded_on) "
	                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	        PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query);

	        for (AttachmentTrn attachmentTrn : attachmentList) {
	            try {
	                String attachmentId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	                attachmentTrn.setAttachment_id(attachmentId);

	                String l_Storage_File_Name = commonUtils.gFN_Upload_File(
	                        attachmentTrn.getL_encrypted_file(),
	                        attachmentTrn.getUploaded_file_name(),
	                        l_File_Path,
	                        attachmentTrn.getEntity_id()
	                );

	                l_PreparedStatement.setString(1, attachmentId);
	                l_PreparedStatement.setString(2, attachmentTrn.getEntity_type());
	                l_PreparedStatement.setString(3, attachmentTrn.getEntity_id());
	                l_PreparedStatement.setString(4, l_File_Path);
	                l_PreparedStatement.setString(5, l_Storage_File_Name);
	                l_PreparedStatement.setString(6, attachmentTrn.getUploaded_file_name());
	                l_PreparedStatement.setString(7, attachmentTrn.getUploaded_by());
	                l_PreparedStatement.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

	                int rowsAffected = l_PreparedStatement.executeUpdate();

	                if (rowsAffected > 0) {
	                    responseMessages.add(new ResponseMessage("Success", "Uploaded successfully", attachmentId));
	                } else {
	                    responseMessages.add(new ResponseMessage("Failure", "Upload failed", attachmentTrn.getUploaded_file_name()));
	                }

	            } catch (Exception ex) {
	                responseMessages.add(new ResponseMessage("Error", ex.getMessage(), attachmentTrn.getUploaded_file_name()));
	            }
	        }

	        return ResponseEntity.status(HttpStatus.CREATED).body(responseMessages);

	    } catch (Exception e) {
	        return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	    } finally {
	        if (l_DBConnection != null) {
	            try {
	                l_DBConnection.close();
	            } catch (Exception e) {
	                return commonUtils.responseErrorHeader(e, "DAO", HttpStatus.INTERNAL_SERVER_ERROR, null);
	            }
	        }
	    }
	}

	public ResponseEntity<Object> getAttachment(CommonRequestModel request) {
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();

		try {
			l_DBConnection = l_DataSource.getConnection();

			String l_Query = "SELECT * FROM attachment_trn WHERE entity_id = '" + request.getId()
					+ "' AND entity_type='" + request.getEntity_type() + "'";

			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(l_Query,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);

			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "NO DATA FOUND");
			} else {
				TypeReference<List<AttachmentTrn>> typeReference = new TypeReference<List<AttachmentTrn>>() {
				};
				List<AttachmentTrn> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(), typeReference);
				for (AttachmentTrn attachment : l_data_List) {
					String encrypted = CommonUtils.gFN_Common_Download_Data(
							attachment.getFile_path() + File.separator + attachment.getStored_file_name());
					attachment.setL_encrypted_file(encrypted);
				}

				return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
			}
		} catch (Exception e) {
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
