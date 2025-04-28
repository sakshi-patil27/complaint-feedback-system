package com.complaintandfeedback.securityConfig;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.complaintandfeedback.DTO.AuthenticationUserDTO;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class JdbcUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        String sql = """
                SELECT u.email, u.password, r.roles_name 
                FROM account_user_mst u
                INNER JOIN roles_mst r ON u.role_id = r.role_id
                WHERE u.email = ?
                """;

        AuthenticationUserDTO user = jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
            AuthenticationUserDTO u = new AuthenticationUserDTO();
            u.setEmail(rs.getString("email"));
            u.setPassword(rs.getString("password"));
            u.setRoleName(rs.getString("roles_name"));
            return u;
        });

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(() -> "ROLE_" + user.getRoleName()))
                .build();
    }
}
