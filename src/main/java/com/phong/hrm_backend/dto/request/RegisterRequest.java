package com.phong.hrm_backend.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    String username;
    String password;
}
