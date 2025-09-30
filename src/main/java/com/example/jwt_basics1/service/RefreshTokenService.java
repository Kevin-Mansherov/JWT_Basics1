package com.example.jwt_basics1.service;

import com.example.jwt_basics1.config.JwtUtil;
import com.example.jwt_basics1.dto.AuthenticationResponse;
import com.example.jwt_basics1.dto.RefreshTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlackListService tokenBlackListService;

    private final Map<String,String> refreshTokenIps = new ConcurrentHashMap<>();

    public void storeRefreshTokenIp(String jwtId, String ip){
        refreshTokenIps.put(jwtId,ip);
    }

    public AuthenticationResponse refreshAccessToken(RefreshTokenRequest request){

        String refreshToken = request.getRefreshToken();
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        String jwtId = jwtUtil.extractJwtId(refreshToken);

        String clientIp = request.getIp();
        String storedIp = refreshTokenIps.get(jwtId);

        // Validate the IP address
        if(!clientIp.equals(storedIp)){
            throw new RuntimeException("Invalid IP address for this refresh token.");
        }

        // Validate the refresh token
        if(!jwtUtil.validateToken(refreshToken,userDetails) || tokenBlackListService.isBlackListed(jwtId)){
            throw new RuntimeException("Invalid refresh token");
        }

        // Blacklist the used refresh token
        Date expiration = jwtUtil.extractExpiration(refreshToken);
        tokenBlackListService.addToBlackList(jwtId, expiration);

        // Generate a new JWT ID
        String newJwtId = UUID.randomUUID().toString();

        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(null,userDetails,newJwtId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails,newJwtId);

        // Store the IP address with the new refresh token
        storeRefreshTokenIp(newJwtId, clientIp);

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }
}
