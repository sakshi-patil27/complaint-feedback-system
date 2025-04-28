package com.complaintandfeedback.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.ComplaintAndFeedbackApplication;
import com.complaintandfeedback.DTO.AuthenticationResponse;
import com.complaintandfeedback.DTO.UpdateUserRequest;
import com.complaintandfeedback.Model.AccountUser;
import com.complaintandfeedback.Model.ResponseMessage;
import com.complaintandfeedback.securityConfig.JwtTokenProvider;

@Service
public class AuthenticationService {

	private final ComplaintAndFeedbackApplication complaintAndFeedbackApplication;
	
	@Autowired
	private CommonUtils commonUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

//	@Autowired
//	private AuthenticationManager authenticationManager;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

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
		
		// Generate accountId
		String accountId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
		accountUser.setAccountId(accountId);
		accountUser.setCreatedOn(LocalDateTime.now().format(formatter));
		accountUser.setIsActive("YES");
		accountUser.setPassword(passwordEncoder.encode(accountUser.getPassword()));
		// Insert user
		String insertQuery = "INSERT INTO account_user_mst (account_id, name, email, phone_no, password, department_id, role_id, org_id, opr_id, created_by, created_on, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(insertQuery, accountUser.getAccountId(), accountUser.getName(), accountUser.getEmail(),
				accountUser.getPhoneNo(), accountUser.getPassword(), accountUser.getDepartmentId(),
				accountUser.getRoleId(), accountUser.getOrgId(), accountUser.getOprId(), accountUser.getCreatedBy(),
				accountUser.getCreatedOn(), accountUser.getIsActive());

		return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessage("Success","User registered successfully",null));
	}
	
	public ResponseEntity<?> login(String email, String password) {
		String sql = "SELECT * FROM account_user_mst WHERE email = ? AND is_active = 'YES'";

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

				AuthenticationResponse response = new AuthenticationResponse(true, "Login successful", token,
						(String) userMap.get("account_id"), (String) userMap.get("role_id"),
						(String) userMap.get("name"));

				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				return commonUtils.responseErrorHeader(null, null, HttpStatus.UNAUTHORIZED, "Invalid credentials");
			}
		} catch (Exception e) {
			return commonUtils.responseErrorHeader(null, null, HttpStatus.UNAUTHORIZED,e.toString());
		}
	}

	public ResponseEntity<?> updateUser(UpdateUserRequest updateUserRequest) {
		String updateQuery = "UPDATE account_user_mst SET name = ?, phone_no = ?, department_id = ?, role_id = ?, modified_by = ?, modified_on = ? WHERE account_id = ?";

		int updated = jdbcTemplate.update(updateQuery, updateUserRequest.getName(), updateUserRequest.getPhoneNo(),
				updateUserRequest.getDepartmentId(), updateUserRequest.getRoleId(), updateUserRequest.getModifiedBy(),
				LocalDateTime.now().format(formatter), updateUserRequest.getAccountId());

		if (updated > 0) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseMessage("Success","User updated successfully",null));
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
        // SQL query to fetch the user by email and ensure they are active
    	String sql = "SELECT * FROM account_user_mst WHERE email = ? AND is_active = 'YES'";

		try {
			Map<String, Object> userMap = jdbcTemplate.queryForMap(sql, email);
            // Check if the user exists
            if (userMap != null) {
                // Encode the new password using password encoder (e.g., bcrypt)
                String encodedPassword = passwordEncoder.encode(newPassword);

                // SQL query to update the user's password
                String updateSql = "UPDATE account_user_mst SET password = ? WHERE email = ? AND is_active = 'YES'";

                // Execute the update query using jdbcTemplate
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


}
