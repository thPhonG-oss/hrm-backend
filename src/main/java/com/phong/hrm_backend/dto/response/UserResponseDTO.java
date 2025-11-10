package com.phong.hrm_backend.dto.response;

import com.phong.hrm_backend.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    Long id;
    String username;
    UserRole role;
    String accessToken;
}
