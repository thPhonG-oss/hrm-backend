package com.phong.hrm_backend.controller;

import com.phong.hrm_backend.dto.response.StravaAuthUrlResponse;
import com.phong.hrm_backend.service.StravaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/connect")
public class StravaController {

    @Autowired
    private StravaService stravaService;

    @GetMapping("/strava")
    public StravaAuthUrlResponse getStravaUrl(){
        String authUrl = stravaService.getAuthorizationUrl();
        return new StravaAuthUrlResponse(authUrl);
    }
}
