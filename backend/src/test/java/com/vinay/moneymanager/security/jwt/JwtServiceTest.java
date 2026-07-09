package com.vinay.moneymanager.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "mySecretKeyForJwtTestingThatIsAtLeast32CharactersLong";
    private static final long EXPIRATION = 3600000L;
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("user1", "password", List.of());
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void shouldExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void shouldNotValidateTokenForIncorrectUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails anotherUser = new User("abc@test.com", "password", List.of());
        boolean tokenValid = jwtService.isTokenValid(token, anotherUser);
        assertFalse(tokenValid);
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        String token = "RandomString";
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        JwtService shortJwtService = new JwtService();
        ReflectionTestUtils.setField(shortJwtService, "secret", SECRET);
        ReflectionTestUtils.setField(shortJwtService, "expiration", -1);
        String token = shortJwtService.generateToken(userDetails);
        assertThrows(ExpiredJwtException.class, () -> shortJwtService.extractUsername(token));

    }

    @Test
    void shouldHaveExpiration() {
        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }


}
