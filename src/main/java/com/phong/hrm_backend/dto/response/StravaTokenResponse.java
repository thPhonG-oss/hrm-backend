package com.phong.hrm_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StravaTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_at")
    private Long expiresAt;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("athlete")
    private StravaAthlete athlete;

    @Data
    public static class StravaAthlete {
        private Long id;
        private String username;

        @JsonProperty("firstname")
        private String firstName;

        @JsonProperty("lastname")
        private String lastName;
    }
}
