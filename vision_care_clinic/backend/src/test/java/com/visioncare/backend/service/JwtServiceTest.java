package com.visioncare.backend.service;

import com.visioncare.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    void testGenerateToken_Success() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testGenerateToken_ContainsUserEmail() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        assertEquals("john.doe@example.com", username);
    }

    @Test
    void testExtractUsername_Success() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);

        assertNotNull(username);
        assertEquals("john.doe@example.com", username);
    }

    @Test
    void testExtractExpiration_Success() {
        String token = jwtService.generateToken(testUser);
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Expiration should be in the future
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        // Test that a newly generated token is not expired by checking expiration date
        String token = jwtService.generateToken(testUser);
        Date expiration = jwtService.extractExpiration(token);
        Date now = new Date();

        // Token should not be expired (expiration should be in the future)
        assertTrue(expiration.after(now));
        
        // Validate token should return true for non-expired token
        Boolean isValid = jwtService.validateToken(token, testUser);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_Success() {
        String token = jwtService.generateToken(testUser);
        Boolean isValid = jwtService.validateToken(token, testUser);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUser() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");

        String token = jwtService.generateToken(testUser);
        Boolean isValid = jwtService.validateToken(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setFirstName("User1");

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setFirstName("User2");

        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", jwtService.extractUsername(token1));
        assertEquals("user2@example.com", jwtService.extractUsername(token2));
    }

    @Test
    void testExtractClaim_FirstName() {
        String token = jwtService.generateToken(testUser);
        String firstName = jwtService.extractClaim(token, claims -> claims.get("firstName", String.class));

        assertNotNull(firstName);
        assertEquals("John", firstName);
    }
}

