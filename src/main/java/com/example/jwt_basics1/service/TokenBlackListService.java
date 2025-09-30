package com.example.jwt_basics1.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlackListService {
    private final Map<String, Date> blackListedTokens = new ConcurrentHashMap<>();

    // Add a token to the blacklist with its expiration date
    public void addToBlackList(String jwtId, Date expiration) {
        blackListedTokens.put(jwtId, expiration);
    }

    // Check if a token is blacklisted
    public boolean isBlackListed(String jwtId) {
        return blackListedTokens.containsKey(jwtId);
    }


    // Scheduled task to clean up expired tokens every 10 minutes
    @Scheduled(fixedRate = 1000 * 60 * 10)
    private void cleanUpExpiredTokens(){
        Date now = new Date();
        Set<String> keys = blackListedTokens.keySet();
        for(String key : keys){
            Date expiration = blackListedTokens.get(key);
            if(expiration.before(now)){
                blackListedTokens.remove(key);
            }
        }
    }
}
