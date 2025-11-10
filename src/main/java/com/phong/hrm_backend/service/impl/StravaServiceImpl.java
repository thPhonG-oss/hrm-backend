package com.phong.hrm_backend.service.impl;

import com.phong.hrm_backend.dto.response.StravaTokenResponse;
import com.phong.hrm_backend.entity.StravaConnection;
import com.phong.hrm_backend.entity.User;
import com.phong.hrm_backend.repository.StravaConnectionRepository;
import com.phong.hrm_backend.repository.UserRepository;
import com.phong.hrm_backend.service.StravaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StravaServiceImpl implements StravaService {
    private final UserRepository userRepository;
    private final StravaConnectionRepository stravaConnectionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${security.oauth2.client.registration.strava.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.registration.strava.client-secret}")
    private String clientSecret;

    @Value("${security.oauth2.client.registration.strava.redirect-uri}")
    private String redirectUri;

    @Value("${security.oauth2.client.provider.strava.authorization-uri}")
    private String authorizationUri;

    @Value("${security.oauth2.client.provider.strava.token-uri}")
    private String tokenUri;

    @Override
    public String getAuthorizationUrl(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Generating Strava authorization URL for user: {}", username);

        // Build authorization URL
        return authorizationUri +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&approval_prompt=force" +
                "&scope=read,activity:read_all" +
                "&state=" + username; // State để identify user sau khi callback
    }

    /**
     * Step 3: Exchange authorization code for access token
     * @param code - Authorization code từ Strava
     * @param state - Username được gửi trong state parameter
     */
    @Transactional
    @Override
    public void exchangeCodeForToken(String code, String state) {

        String username = state;

        if (username == null || username.isEmpty()) {
            throw new RuntimeException("State parameter (username) is missing");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Exchanging code for token - user: {}", username);

        // Tìm user trong database

        // Prepare request body
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("code", code);
        requestBody.add("grant_type", "authorization_code");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Call Strava Token API
            ResponseEntity<StravaTokenResponse> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    request,
                    StravaTokenResponse.class
            );

            StravaTokenResponse tokenResponse = response.getBody();

            if (tokenResponse == null) {
                throw new RuntimeException("Empty response from Strava");
            }

            log.info("Successfully got tokens from Strava for user: {}", username);

            // Xóa connection cũ nếu có
            stravaConnectionRepository.findByUser(user).ifPresent(entity -> stravaConnectionRepository.delete((StravaConnection) entity));

            // Lưu tokens vào database
            StravaConnection stravaConnection = StravaConnection.builder()
                    .user(user)
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresAt(tokenResponse.getExpiresAt())
                    .build();

            stravaConnectionRepository.save(stravaConnection);

            log.info("Saved Strava connection for user: {}", username);

        } catch (Exception e) {
            log.error("Error exchanging code for token: {}", e.getMessage());
            throw new RuntimeException("Failed to connect Strava: " + e.getMessage());
        }
    }
}
