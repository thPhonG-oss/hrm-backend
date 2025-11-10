package com.phong.hrm_backend.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {
    String username;
    String password;
}
