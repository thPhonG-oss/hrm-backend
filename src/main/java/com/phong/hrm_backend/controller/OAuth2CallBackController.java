package com.phong.hrm_backend.controller;

import com.phong.hrm_backend.service.StravaService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/callback")
public class OAuth2CallBackController {
    private final StravaService stravaService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping
    public void handleStravaCallback(@RequestParam("code") String code,
                                     @RequestParam(value = "scope", required = false) String scope,
                                     @RequestParam(value = "state", required = false) String state,
                                     HttpServletResponse response)throws IOException {
        log.info("Received Strava callback - code: {}, scope: {}", code, scope);

        try {
            // Exchange code for access token và lưu vào DB
            stravaService.exchangeCodeForToken(code, state);

            // Redirect về frontend với success
            response.sendRedirect(frontendUrl + "/dashboard?strava=success");

        } catch (Exception e) {
            log.error("Error handling Strava callback: {}", e.getMessage());

            // Redirect về frontend với error
            response.sendRedirect(frontendUrl + "/dashboard?strava=error&message=" + e.getMessage());
        }

    }
}
