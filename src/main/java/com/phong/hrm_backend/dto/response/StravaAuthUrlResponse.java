package com.phong.hrm_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StravaAuthUrlResponse {
    private String redirectUrl;
}
