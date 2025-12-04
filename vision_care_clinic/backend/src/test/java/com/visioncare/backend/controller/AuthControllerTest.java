package com.visioncare.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visioncare.backend.dto.LoginRequest;
import com.visioncare.backend.dto.LoginResponse;
import com.visioncare.backend.dto.RegisterRequest;
import com.visioncare.backend.model.User;
import com.visioncare.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.visioncare.backend.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.visioncare.backend.config.JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

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
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Error: Email is already in use!"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Email is already in use!"));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        String testToken = "test-jwt-token";
        when(authService.login(any(LoginRequest.class))).thenReturn(testToken);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken));
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Error: Invalid email or password"));
    }

    @Test
    void testLoginUser_EmptyEmail() throws Exception {
        loginRequest.setEmail("");
        
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterUser_MissingFields() throws Exception {
        registerRequest.setEmail(null);
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email is required"));
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}

