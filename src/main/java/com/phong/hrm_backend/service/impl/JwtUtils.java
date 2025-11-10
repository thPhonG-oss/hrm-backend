package com.phong.hrm_backend.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-seconds}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-validity-seconds}")
    private Long refreshTokenExpiration;

    @Value("${spring.app.jwtCookie}")
    private String jwtCookie;

    public String generateAccessToken(String username){
        return generateToken(username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username){
        return generateToken(username, refreshTokenExpiration);
    }

    public ResponseCookie generateJwtCookie(String username, Long expiration){
        String token = generateToken(username, refreshTokenExpiration);
        return ResponseCookie.from(jwtCookie, token)
                .path("/api")
                .httpOnly(false)
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    public ResponseCookie createRefreshCookie(String refreshToken){
        return ResponseCookie.from(jwtCookie, refreshToken)
                .path("/api")
                .httpOnly(false)
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    public ResponseCookie clearCookie(){
        return ResponseCookie.from(jwtCookie, null)
                .path("/api")
                .httpOnly(false)
                .sameSite("Lax")
                .maxAge(Duration.ofDays(0))
                .build();
    }

    private String generateToken(String username, Long expiration){
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .expirationTime(Date.from((Instant.now().plus(expiration, ChronoUnit.MILLIS))))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS512),
                    claimsSet
            );

            signedJWT.sign(new MACSigner(secret.getBytes()));
            return signedJWT.serialize();
        }
        catch(JOSEException e){
            throw new RuntimeException("Error generating token", e);
        }
    }



    public String getUsernameFromToken(String token){
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }


    public boolean validateToken(String token){
        try{
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());

            if(!signedJWT.verify(verifier)){
                return false;
            }

            Date expirationDate = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationDate.after(new Date());
        } catch (Exception e){
            return false;
        }
    }

    public String getTokenFromHeader(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }

        return null;
    }

    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        if(cookie != null){
            return cookie.getValue();
        }
        return null;
    }
}
