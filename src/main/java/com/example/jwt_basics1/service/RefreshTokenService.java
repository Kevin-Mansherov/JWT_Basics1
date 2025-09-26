package com.example.jwt_basics1.service;

import com.example.jwt_basics1.config.JwtUtil;
import com.example.jwt_basics1.dto.AuthenticationResponse;
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

    public AuthenticationResponse refreshAccessToken(String refreshToken){

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if(jwtUtil.validateToken(refreshToken,userDetails)){
            return new AuthenticationResponse(jwtUtil.generateToken(null,userDetails), refreshToken);
        }
        throw new RuntimeException("Invalid refresh token");
    }





//    // In-memory store for refresh tokens.
//    private final Map<String, UUID> inMemoryRefreshTokens = new ConcurrentHashMap<>();
//    private final UserDetailsService userDetailsService;
//
//    // Store a new refresh token with a unique ID
//    public void storeRefreshToken(String refreshToken){
//        UUID tokenId = UUID.randomUUID();
//        inMemoryRefreshTokens.put(refreshToken, tokenId);
//    }
//
//    public AuthenticationResponse refreshToken(String oldRefreshToken){
//        // Extract the token ID from the old refresh token
//        UUID tokenId = inMemoryRefreshTokens.get(oldRefreshToken);
//
//        //Checks if the token exists in the store
//        if(tokenId == null){
//            throw new RuntimeException("Refresh token not recognized or revoked.");
//        }
//
//        String username;
//
//        try{
//            username = jwtUtil.extractUsername(oldRefreshToken);
//
//            //check if token is expired
//            if(jwtUtil.extractExpiration(oldRefreshToken).before(new Date())){
//                deleteTokenEntry(oldRefreshToken);
//                throw new RuntimeException("Refresh token expired.");
//            }
//        }catch (Exception e){
//            deleteTokenEntry(oldRefreshToken);
//            throw new RuntimeException("Invalid refresh token structure or signature.");
//        }
//
//        // Load user details
//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//        //Creates a new access token.
//        String newAccessToken = jwtUtil.generateToken(null, userDetails);
//        //Creates a new refresh token.
//        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
//
//        //Deletes the old refresh token.
//        deleteTokenEntry(oldRefreshToken);
//        //Stores the new refresh token.
//        storeRefreshToken(newRefreshToken);
//
//        return new AuthenticationResponse(newAccessToken, newRefreshToken);
//    }
//
//    private void deleteTokenEntry(String refreshToken){
//        inMemoryRefreshTokens.remove(refreshToken);
//    }
//
//    private void deleteRefreshToken(String refreshToken){
//        deleteTokenEntry(refreshToken);
//    }
}
