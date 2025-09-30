package com.example.jwt_basics1.config;

import com.example.jwt_basics1.dto.AuthenticationRequest;
import com.example.jwt_basics1.service.RefreshTokenService;
import com.example.jwt_basics1.service.TokenBlackListService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class JwtUtil {

    private Key key;  // Store the generated key in a
    private final TokenBlackListService tokenBlackListService;

    @PostConstruct
    public void init() {
        try {
            // private final String SECRET_KEY = JwtProperties.SECRET;
            KeyGenerator secretKeyGen = KeyGenerator.getInstance("HmacSHA256");
            this.key = Keys.hmacShaKeyFor(secretKeyGen.generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    private Key getKey() {
        return this.key;  // Use the stored key
    }

    // Generate a JWT token for a user, first time login
    public String generateToken(AuthenticationRequest authenticationRequest,
                                UserDetails userDetails, String jwtId) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setId(jwtId)  // Set the JWT ID
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails, String jwtId) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .setId(jwtId)  // Set the JWT ID
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JwtProperties.REFRESH_EXPIRATION_TIME))
                .signWith(getKey())
                .compact();
    }


    // Extract the expiration date from a JWT token and implicitly validate the token
    // This implementation implicitly validates the signature when extracting claims:
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            // extract the username from the JWT token, and check the signature and the expiration
            String username = extractUsername(token);
            // Extract the JWT ID from the token.
            String jwtId = extractJwtId(token);

            if(tokenBlackListService.isBlackListed(jwtId)){
                System.out.println("\nOperation in JwtUtil.ValidateToken: Token is blacklisted.\n");
                return false;
            }

            // check if the username extracted from the JWT token matches the username in the UserDetails object
            // and the token is not expired
            // Also check if the token is blacklisted
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            System.out.println("\nOperation in JwtUtil.ValidateToken: Token has expired: " + e.getMessage() + "\n");
            return false;
        } catch(Exception e){
            System.out.println("\nOperation in JwtUtil.ValidateToken: Exception during token validation: " + e.getMessage() + "\n");
            return false;
        }
    }

    // Extract the username from a JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJwtId(String token){
        return extractAllClaims(token).getId();
    }

    private <T> T extractClaim(String string, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(string);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from a JWT token
    private Claims extractAllClaims(String token) {
        SecretKey secretKey = (SecretKey) getKey();
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build().parseSignedClaims(token).getPayload();
    }

    // Check if a JWT token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    // Extract the expiration date from a JWT token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
