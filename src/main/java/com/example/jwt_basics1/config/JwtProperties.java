package com.example.jwt_basics1.config;

public class JwtProperties {

    // The EXPIRATION_TIME constant is used to set the expiration time of the JWT
    // 5 minutes, it is recommended to set this to 30 minutes

//    public static final int EXPIRATION_TIME = 300_000;

    public static final int EXPIRATION_TIME = 60_000; // 1 minute for testing the refresh token


    // The TOKEN_PREFIX constant is used to prefix the JWT in the Authorization header
    public static final String TOKEN_PREFIX = "Bearer ";

    // The HEADER_STRING constant is used to set the key of the Authorization header
    public static final String HEADER_STRING = "Authorization";

    public static final int REFRESH_EXPIRATION_TIME = 350_000; // 35 minutes

}
