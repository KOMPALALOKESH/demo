package com.example.demo.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long expirationTimeMs;

    /**
     * Gets the secret signing key used for JWT creation and parsing.
     * Used internally by extractAllClaims(), createToken(), and any place a signature is required.
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Extracts all claims present in the JWT token.
     * Uses getSignKey() for digital signature verification.
     * This function is the backbone for extracting specific claims or details from the token,
     * and is used by extractClaim().
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generic function to extract any specific claim from the JWT, using a claimsResolver function.
     * Directly depends on extractAllClaims() to obtain the Claims object.
     * Used by more specific extraction functions like extractUsername() and extractExpiration().
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the username (subject) from the JWT token.
     * Relies on extractClaim() with Claims::getSubject for extracting the subject.
     * Used in isTokenValid() and typically required in authentication flows.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Creates and signs a JWT token containing the claims and subject (typically username).
     * Uses getSignKey() for signing the token and sets standard fields like issuedAt and expiration.
     * Used internally by generateToken().
     */
    public String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(getSignKey())
                .compact();
    }
    
    /**
     * Generates a JWT token for a given UserDetails instance.
     * Builds a claims map and delegates the token creation to createToken().
     * Generally invoked for issuing access tokens after successful authentication.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Adds authorities (roles) as part of the claims for further use/verification.
        claims.put("role", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername()); 
    }

    /**
     * Extracts the expiration date from the JWT.
     * Uses extractClaim() with Claims::getExpiration.
     * Utilized in isTokenExpired() to determine token validity based on time.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the token is expired by comparing expiration date with current time.
     * Relies on extractExpiration().
     * Used internally by isTokenValid() as part of validation logic.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Validates the JWT token for a given UserDetails object.
     * Checks username in the token equals the one in UserDetails and ensures the token is not expired.
     * Uses extractUsername() and isTokenExpired().
     * Catches all exceptions and returns false if validation fails at any point.
     * Frequently used in filters or security logic to confirm a token's authenticity and freshness.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

}
