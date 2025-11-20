package com.visioncare.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visioncare.backend.model.User;
import com.visioncare.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@Import(com.visioncare.backend.controller.GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private com.visioncare.backend.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.visioncare.backend.config.JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhone("1234567890");
        testUser.setDateOfBirth("1990-01-01");
        testUser.setHasPet(false);
    }

    @Test
    void testGetMyProfile_Success() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/auth/me")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testGetMyProfile_UserNotFound() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.empty());

        // UsernameNotFoundException will be thrown and converted to 404 by exception handler
        mockMvc.perform(get("/api/auth/me")
                .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User not found with email: john.doe@example.com"));
    }

    @Test
    void testGetMyProfile_Unauthorized() throws Exception {
        // Without Principal, the controller will get null and throw NullPointerException
        // This will result in a 500 error via exception handler
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testUpdateUserProfile_Success() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setPhone("9876543210");
        updatedUser.setDateOfBirth("1995-05-15");
        updatedUser.setHasPet(true);
        updatedUser.setPetType("Dog");
        updatedUser.setPetName("Buddy");
        updatedUser.setPetFavoriteTreat("Biscuits");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("Jane");
        savedUser.setLastName("Smith");
        savedUser.setPhone("9876543210");
        savedUser.setDateOfBirth("1995-05-15");
        savedUser.setHasPet(true);
        savedUser.setPetType("Dog");
        savedUser.setPetName("Buddy");
        savedUser.setPetFavoriteTreat("Biscuits");
        savedUser.setEmail("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(put("/api/auth/profile")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.hasPet").value(true))
                .andExpect(jsonPath("$.petType").value("Dog"))
                .andExpect(jsonPath("$.petName").value("Buddy"));
    }

    @Test
    void testUpdateUserProfile_UserNotFound() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");

        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/auth/profile")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Error: User not found with email: john.doe@example.com"));
    }

    @Test
    void testUpdateUserProfile_PartialUpdate() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Doe"); // Keep existing value
        updatedUser.setPhone("1234567890"); // Keep existing value
        updatedUser.setDateOfBirth("1990-01-01"); // Keep existing value
        updatedUser.setHasPet(false); // Keep existing value
        // Only updating first name, other fields remain unchanged

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("Jane");
        savedUser.setLastName("Doe");
        savedUser.setPhone("1234567890");
        savedUser.setDateOfBirth("1990-01-01");
        savedUser.setHasPet(false);
        savedUser.setEmail("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(put("/api/auth/profile")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe")); // Unchanged
    }

    @Test
    void testUpdateUserProfile_Unauthorized() throws Exception {
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");

        // Without Principal, the controller will get null and throw NullPointerException
        // which will be caught and return 400
        mockMvc.perform(put("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isBadRequest());
    }
}

