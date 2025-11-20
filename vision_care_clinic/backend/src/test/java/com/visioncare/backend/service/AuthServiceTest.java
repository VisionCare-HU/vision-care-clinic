package com.visioncare.backend.service;

import com.visioncare.backend.dto.LoginRequest;
import com.visioncare.backend.dto.RegisterRequest;
import com.visioncare.backend.model.User;
import com.visioncare.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("1234567890");
        registerRequest.setDateOfBirth("1990-01-01");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setPhone("1234567890");
        testUser.setDateOfBirth("1990-01-01");
    }

    @Test
    void testRegister_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Error: Email is already in use!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        String testToken = "test-jwt-token";
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(testToken);

        String result = authService.login(loginRequest);

        assertEquals(testToken, result);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(testUser);
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testUpdateUserProfile_Success() {
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setPhone("9876543210");
        updatedUser.setDateOfBirth("1995-05-15");
        updatedUser.setHasPet(true);
        updatedUser.setPetType("Dog");
        updatedUser.setPetName("Buddy");
        updatedUser.setPetFavoriteTreat("Biscuits");

        testUser.setFirstName("Jane");
        testUser.setLastName("Smith");
        testUser.setPhone("9876543210");
        testUser.setDateOfBirth("1995-05-15");
        testUser.setHasPet(true);
        testUser.setPetType("Dog");
        testUser.setPetName("Buddy");
        testUser.setPetFavoriteTreat("Biscuits");

        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.updateUserProfile("john.doe@example.com", updatedUser);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(true, result.getHasPet());
        assertEquals("Dog", result.getPetType());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUserProfile_UserNotFound() {
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");

        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> {
            authService.updateUserProfile("nonexistent@example.com", updatedUser);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}

