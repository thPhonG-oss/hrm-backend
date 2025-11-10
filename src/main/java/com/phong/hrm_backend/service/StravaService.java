package com.phong.hrm_backend.service;

import com.phong.hrm_backend.dto.response.StravaAuthUrlResponse;
import jakarta.transaction.Transactional;

public interface StravaService {
    String getAuthorizationUrl();


    @Transactional
    void exchangeCodeForToken(String code, String state);
}
