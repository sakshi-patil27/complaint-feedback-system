package com.complaintandfeedback.Service;


import java.security.Timestamp;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.complaintandfeedback.Model.ResponseHeaderModel;

@Component
public class CommonUtils {
	public ResponseEntity<Object> responseErrorHeader(Exception e, String errorPattern, HttpStatus responseCode,
			String errorMsg) {
		ResponseHeaderModel ResponseHeaderModel = new ResponseHeaderModel();
		ResponseHeaderModel.setRespCode(responseCode.toString());
		ResponseHeaderModel.setStatusMsg("Failed");

		if (e != null) {
			StackTraceElement[] stsckArr = e.getStackTrace();
			String errorClass = e.getMessage();
			if (errorPattern != null && !errorPattern.trim().equals("")) {
				for (StackTraceElement stkline : stsckArr) {
					if (stkline.getFileName()!=null && stkline.getFileName().contains(errorPattern)) {
						errorClass = stkline.getFileName()+" "+stkline.getMethodName() + " Line(" + stkline.getLineNumber() + ")";
						break;
					}
				}
			}

			ResponseHeaderModel.setErrMsg(errorClass + "*--*--" + e.toString());
		} else {
			ResponseHeaderModel.setErrMsg(errorMsg == null ? "Bad request" : errorMsg);
		}

		return ResponseEntity.status(responseCode).body(ResponseHeaderModel);
	}

	public static JSONArray convertToJsonArray(ResultSet resultSet, int notrequiredcols) throws Exception {
		JSONArray jsonArray = new JSONArray();

		while (resultSet.next()) {
			int columns = resultSet.getMetaData().getColumnCount();
			columns -= notrequiredcols;
			JSONObject obj = new JSONObject();
			int j = 0;
			for (int i = 0; i < columns; i++) {
				j = i;
				Object value = resultSet.getObject(j + 1);
				String clas = "";
				if (!isNull(value)) {
					clas = value.getClass().toString();
				}
				
				if (resultSet.getObject(i + 1) instanceof LocalDateTime || clas.toUpperCase().contains("LOCALDATETIME")) {
					String l_formattedDateTimeString =parseDateTime(resultSet.getObject(i + 1).toString());
					if (resultSet.getObject(i + 1) == null)
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), l_formattedDateTimeString);
					else {
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), l_formattedDateTimeString);
					}
				}
				else if (resultSet.getObject(i + 1) instanceof Time || clas.toUpperCase().contains("TIME")) {
					if (resultSet.getObject(i + 1) == null)
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1));
					else {
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1).toString());
					}
				}else if (resultSet.getObject(i + 1) instanceof Date || clas.toUpperCase().contains("DATE")) {
					if (resultSet.getObject(i + 1) == null)
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1));
					else {
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1).toString());
					}
				} else if (resultSet.getObject(i + 1) instanceof Timestamp
						|| clas.toUpperCase().contains("TIMESTAMP")) {
					if (resultSet.getObject(i + 1) == null) {
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1));
					} else {
						String date = resultSet.getObject(i + 1).toString();
//						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), date.contains(".") ? date.substring(0, date.indexOf(".")) : date);
						obj.put(resultSet.getMetaData().getColumnLabel(i + 1), date);// .contains(".") ?
																						// date.substring(0,
																						// date.indexOf(".")) : date);
//						 SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//						 Date l_date = (Date) resultSet.getObject(i + 1);
//						 obj.put(resultSet.getMetaData().getColumnLabel(i + 1), dateFormat.format(l_date));
					}
				} else {
					obj.put(resultSet.getMetaData().getColumnLabel(i + 1), resultSet.getObject(i + 1));
				}
				j = i;
			}

			jsonArray.put(obj);
		}

		return jsonArray;
	}
	
	public static String parseDateTime(String dateTime) {
    	String dateTimeConverted = "";
    	
    	List<DateTimeFormatter> formattersToCheck = new ArrayList<>();
    	formattersToCheck.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    	formattersToCheck.add(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (DateTimeFormatter formatter : formattersToCheck) {
            try {
            	LocalDateTime dateTimeToConvert = LocalDateTime.parse(dateTime, formatter);
            	if(isNull(dateTimeToConvert)) {
            		dateTimeConverted = dateTimeToConvert.format(targetFormatter);
            	}
            } catch (DateTimeParseException e) {
                // Continue to try the next formatter
            }
        }
		return dateTimeConverted;
    }
	public static boolean isNull(Object object) {
		boolean isValid = false;
		if (object == null) {
			isValid = true;
		} else if (object instanceof String && ((String) object).trim().equals("")) {
			isValid = true;
		}

		return isValid;
	}
}
