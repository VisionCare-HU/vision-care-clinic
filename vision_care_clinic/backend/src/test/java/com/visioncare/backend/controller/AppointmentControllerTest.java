package com.visioncare.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visioncare.backend.model.Appointment;
import com.visioncare.backend.model.User;
import com.visioncare.backend.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AppointmentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@Import(com.visioncare.backend.controller.GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private com.visioncare.backend.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.visioncare.backend.config.JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Appointment testAppointment;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("john.doe@example.com");
        testUser.setPhone("1234567890");

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setFirstName("John");
        testAppointment.setLastName("Doe");
        testAppointment.setEmail("john.doe@example.com");
        testAppointment.setPhone("1234567890");
        testAppointment.setDate(LocalDate.now().plusDays(1).toString());
        testAppointment.setTime("10:00");
        testAppointment.setService("Comprehensive Eye Exam");
        testAppointment.setUser(testUser);
    }

    @Test
    void testCreateAppointment_Success() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        // Use notNull() to match only non-null Principal
        when(appointmentService.createAppointment(any(Appointment.class), notNull(Principal.class)))
                .thenReturn(testAppointment);

        mockMvc.perform(post("/api/appointments/book")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.service").value("Comprehensive Eye Exam"));
    }

    @Test
    void testCreateAppointment_MissingRequiredFields() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        testAppointment.setFirstName(null);

        // Service will throw IllegalArgumentException when validating
        when(appointmentService.createAppointment(any(Appointment.class), notNull(Principal.class)))
                .thenThrow(new IllegalArgumentException("First name is required"));

        mockMvc.perform(post("/api/appointments/book")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("First name is required"));
    }

    @Test
    void testGetAllAppointments_Success() throws Exception {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentService.getAllAppointments()).thenReturn(appointments);

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testGetMyAppointments_Success() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentService.getAppointmentsForUser(notNull(Principal.class))).thenReturn(appointments);

        mockMvc.perform(get("/api/appointments/me")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void testGetMyAppointments_EmptyList() throws Exception {
        Principal mockPrincipal = () -> "john.doe@example.com";
        when(appointmentService.getAppointmentsForUser(notNull(Principal.class))).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/appointments/me")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testCreateAppointment_Unauthorized() throws Exception {
        // Without Principal, the service will receive null and throw NPE when calling principal.getName()
        // Mock the service to throw NPE when Principal is null
        doThrow(new NullPointerException("Principal cannot be null"))
                .when(appointmentService).createAppointment(any(Appointment.class), isNull());

        mockMvc.perform(post("/api/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAppointment)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetMyAppointments_Unauthorized() throws Exception {
        // Without Principal, the service will receive null and throw NPE when calling principal.getName()
        // Mock the service to throw NPE when Principal is null
        doThrow(new NullPointerException("Principal cannot be null"))
                .when(appointmentService).getAppointmentsForUser(isNull());

        mockMvc.perform(get("/api/appointments/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").exists());
    }
}

