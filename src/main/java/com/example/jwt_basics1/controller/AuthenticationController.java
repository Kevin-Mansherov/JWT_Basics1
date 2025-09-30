package com.example.jwt_basics1.controller;

import com.example.jwt_basics1.config.JwtUtil;
import com.example.jwt_basics1.dto.AuthenticationRequest;
import com.example.jwt_basics1.dto.AuthenticationResponse;
import com.example.jwt_basics1.dto.RefreshTokenRequest;
import com.example.jwt_basics1.service.AuthenticationService;
import com.example.jwt_basics1.service.RefreshTokenService;
import com.example.jwt_basics1.service.TokenBlackListService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlackListService tokenBlackListService;
    private final JwtUtil jwtUtil;

    // The authenticateUser() method takes in an AuthenticationRequest object, which contains the username and password.
    // The method returns an AuthenticationResponse object, which contains the JWT and refresh token, and the user's roles.
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @RequestBody AuthenticationRequest authenticationRequest,
            HttpServletRequest request) {
        try {
            String clientIp = request.getRemoteAddr();
            authenticationRequest.setIp(clientIp);
            AuthenticationResponse authResponse = authenticationService.authenticate(authenticationRequest);
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationServiceException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest) {
        //Return bad request if the refresh token is missing
        if(request == null || request.getRefreshToken() == null || request.getRefreshToken().isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        try{
            String clientIp = httpServletRequest.getRemoteAddr();
            request.setIp(clientIp);
            // Call the refreshToken method from the RefreshTokenService
            AuthenticationResponse response = refreshTokenService.refreshAccessToken(request);
            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            System.out.println("Refresh token error: " + e.getMessage());
            // If the refresh token is invalid or expired, return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}