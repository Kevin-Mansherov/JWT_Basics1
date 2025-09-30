package com.example.jwt_basics1.service;

import com.example.jwt_basics1.config.JwtProperties;
import com.example.jwt_basics1.config.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;


@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenBlackListService tokenBlackListService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try{
            // Extract the token from the Authorization header
            String header = request.getHeader(JwtProperties.HEADER_STRING);

            if(header != null && header.startsWith(JwtProperties.TOKEN_PREFIX)){
                String token = header.substring(JwtProperties.TOKEN_PREFIX.length());

                String jwtId = jwtUtil.extractJwtId(token);
                Date expiration = jwtUtil.extractExpiration(token);

                tokenBlackListService.addToBlackList(jwtId, expiration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Logout successful. Token blacklisted.");
            }else{
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("No access token provided.");
            }
        }catch(Exception ex){
            try{
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Logout failed: " + ex.getMessage());
            }catch(IOException ex2){
                throw new RuntimeException(ex2);
            }
        }
    }
}
