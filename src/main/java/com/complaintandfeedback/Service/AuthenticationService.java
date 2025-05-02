package com.complaintandfeedback.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.ComplaintAndFeedbackApplication;
import com.complaintandfeedback.DTO.AuthenticationResponse;
import com.complaintandfeedback.DTO.CommonRequestModel;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Model.ResponseMessage;
import com.complaintandfeedback.securityConfig.JwtTokenProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthenticationService {

	private final ComplaintAndFeedbackApplication complaintAndFeedbackApplication;
	
	@Autowired
	private CommonUtils commonUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private DataSource l_DataSource;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    @Autowired
    private EmailService emailService;

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	AuthenticationService(ComplaintAndFeedbackApplication complaintAndFeedbackApplication) {
		this.complaintAndFeedbackApplication = complaintAndFeedbackApplication;
	}

	public ResponseEntity<?> registerUser(AccountUser accountUser) {
		// Check if email already exists
		String emailQuery = "SELECT COUNT(*) FROM account_user_mst WHERE email = ?";
		Integer count = jdbcTemplate.queryForObject(emailQuery, Integer.class, accountUser.getEmail());
		
		if (count != null && count > 0) {	
			return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST, "Email already exists");
		}
		
//		String roleIdQuery = "SELECT role_id FROM roles_mst WHERE LOWER(role_name) = LOWER(?) AND org_id = ? AND opr_id = ?";
//		Integer deptHeadRoleId = jdbcTemplate.queryForObject(roleIdQuery, Integer.class, "DEPARTMENT_HEAD",accountUser.getOrgId(),accountUser.getOprId());
//		// Step 3: If user is trying to register as department head, check if one already exists
//		if (Objects.equals(accountUser.getRoleId(), deptHeadRoleId)) {
//			String headExistsQuery = "SELECT COUNT(*) FROM account_user_mst WHERE department_id = ? AND role_id = ? AND org_id = ? AND opr_id = ?";
//			Integer headCount = jdbcTemplate.queryForObject(
//					headExistsQuery,
//					Integer.class,
//					accountUser.getDepartmentId(),
//					deptHeadRoleId,
//					accountUser.getOrgId(),
//					accountUser.getOprId()
//			);
//			if (headCount != null && headCount > 0) {
//				return commonUtils.responseErrorHeader(null, null, HttpStatus.CONFLICT,
//						"Department Head already exists for this department in the same organization and operator");
//			}
//		}
//
//		
		// Generate accountId
		String accountId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		accountUser.setAccount_id(accountId);
		accountUser.setCreated_on(LocalDateTime.now().format(formatter));
		accountUser.setIs_active("YES");
		accountUser.setPassword(passwordEncoder.encode(accountUser.getPassword()));
		// Insert user
		String insertQuery = "INSERT INTO account_user_mst (account_id, name, email, phone_no, password, department_id, role_id, org_id, opr_id, created_by, created_on, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(insertQuery, accountUser.getAccount_id(), accountUser.getName(), accountUser.getEmail(),
				accountUser.getPhone_no(), accountUser.getPassword(), accountUser.getDepartment_id(),
				accountUser.getRole_id(), accountUser.getOrg_id(), accountUser.getOpr_id(), accountUser.getCreated_by(),
				accountUser.getCreated_on(), accountUser.getIs_active());

		return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("Success","User registered successfully",accountId));
	}
	
	public ResponseEntity<?> login(String email, String password) {
		String sql = "SELECT a.account_id,a.password,a.org_id,a.opr_id,a.role_id,a.name,org.org_name as l_org_name,roles_mst.role_name AS l_role_name "
				+ "FROM account_user_mst a"
				+ " LEFT JOIN org ON org.org_id=a.org_id"
				+ " LEFT JOIN roles_mst ON roles_mst.role_id=a.role_id"
				+ " WHERE a.email = ? AND a.is_active = 'YES'";

		try {
			Map<String, Object> userMap = jdbcTemplate.queryForMap(sql, email);
			if (userMap != null) {
				String storedPassword = (String) userMap.get("password");

				if (!passwordEncoder.matches(password, storedPassword)) {
					throw new BadCredentialsException("Invalid credentials");
				}

				Authentication authentication = new UsernamePasswordAuthenticationToken(email, null,
						List.of(new SimpleGrantedAuthority((String) userMap.get("role_id"))));

				String token = jwtTokenProvider.generateToken(authentication);
				             Long orgId = userMap.get("org_id") != null ? ((Number) userMap.get("org_id")).longValue() : null;
			                Long oprId = userMap.get("opr_id") != null ? ((Number) userMap.get("opr_id")).longValue() : null;
				AuthenticationResponse response = new AuthenticationResponse(true, "Login successful", token,
						(String) userMap.get("account_id"), (String) userMap.get("role_id"),
						(String) userMap.get("name"),orgId,oprId,(String) userMap.get("l_org_name"),(String) userMap.get("l_role_name"));

				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.UNAUTHORIZED, "Invalid credentials");
			}
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(null, null, HttpStatus.UNAUTHORIZED,e.toString());
		}
	}

	public ResponseEntity<?> updateUser(AccountUser updateUserRequest) {
		String updateQuery = "UPDATE account_user_mst SET name = ?, phone_no = ?, department_id = ?, role_id = ?, modified_by = ?, modified_on = ?  ,password = ? WHERE account_id = ?";
		updateUserRequest.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
		int updated = jdbcTemplate.update(updateQuery, updateUserRequest.getName(), updateUserRequest.getPhone_no(),
				updateUserRequest.getDepartment_id(), updateUserRequest.getRole_id(), updateUserRequest.getModified_by(),
				LocalDateTime.now().format(formatter),updateUserRequest.getPassword(), updateUserRequest.getAccount_id());

		if (updated > 0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseMessage("Success","User updated successfully",updateUserRequest.getAccount_id()));
		} else {
			return commonUtils.responseErrorHeader(null, null, HttpStatus.NOT_FOUND,"User not found");
		}
	}
	
	

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(email, otp);

        // Send the OTP
        emailService.sendOtpEmail(email, otp);

        // Remove OTP after 5 minutes
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            otpStorage.remove(email);
        }, 5, TimeUnit.MINUTES);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        return otp.equals(otpStorage.get(email));
    }

    public boolean resetPassword(String email, String newPassword) {
    	String sql = "SELECT * FROM account_user_mst WHERE email = ? AND is_active = 'YES'";

		try {
			Map<String, Object> userMap = jdbcTemplate.queryForMap(sql, email);
            if (userMap != null) {
                String encodedPassword = passwordEncoder.encode(newPassword);

                String updateSql = "UPDATE account_user_mst SET password = ? WHERE email = ? AND is_active = 'YES'";

                int rowsUpdated = jdbcTemplate.update(updateSql, encodedPassword, email);

                // Check if a row was updated (1 row should be updated if the user exists and is active)
                if (rowsUpdated > 0) {
                    return true;  // Password updated successfully
                }
            }
        } catch (EmptyResultDataAccessException e) {
            System.out.println("User not found or is inactive.");
            return false;
        }

        return false; 
    }

	public ResponseEntity<Object> getUserByDepartment(CommonRequestModel request) {
		
		Connection l_DBConnection = null;
		JSONArray l_ModuleArr = new JSONArray();	
		try {
			l_DBConnection = l_DataSource.getConnection();
			String l_Query = "SELECT * FROM account_user_mst WHERE is_active = 'YES' AND "
					+ "org_id = ? AND opr_id = ? AND department_id = ?";
			PreparedStatement l_PreparedStatement = l_DBConnection.prepareStatement(
			        l_Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
			// Use parameter binding to avoid SQL injection
			l_PreparedStatement.setLong(1, request.getOrgId());
			l_PreparedStatement.setLong(2, request.getOprId());
			l_PreparedStatement.setString(3, request.getId());
			
			ResultSet l_ResultSet = l_PreparedStatement.executeQuery();
			l_ModuleArr = CommonUtils.convertToJsonArray(l_ResultSet, 0);
			
			if (l_ModuleArr.isEmpty()) {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.BAD_REQUEST,
						"NO DATA FOUND");
			} else {
				TypeReference<List<AccountUser>> typeReference = new TypeReference<List<AccountUser>>() {
				};
				List<AccountUser> l_data_List = new ObjectMapper().readValue(l_ModuleArr.toString(),
						typeReference);
				return ResponseEntity.status(HttpStatus.OK).body(l_data_List);
			}
		}
		catch (Exception e) {
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
