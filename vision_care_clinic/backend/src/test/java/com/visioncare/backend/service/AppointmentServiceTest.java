package com.visioncare.backend.service;

import com.visioncare.backend.model.Appointment;
import com.visioncare.backend.model.User;
import com.visioncare.backend.repository.AppointmentRepository;
import com.visioncare.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Principal principal;

    @InjectMocks
    private AppointmentService appointmentService;

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
        testAppointment.setDate(LocalDate.now().plusDays(1).toString());
        testAppointment.setTime("10:00");
        testAppointment.setService("Comprehensive Eye Exam");

        // Use lenient() to avoid UnnecessaryStubbingException for tests that don't use principal
        lenient().when(principal.getName()).thenReturn("john.doe@example.com");
    }

    @Test
    void testCreateAppointment_Success() {
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        Appointment result = appointmentService.createAppointment(testAppointment, principal);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertEquals(testUser, result.getUser());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_UserNotFound() {
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_MissingFirstName() {
        testAppointment.setFirstName(null);

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_MissingLastName() {
        testAppointment.setLastName(null);

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_MissingDate() {
        testAppointment.setDate(null);

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_MissingTime() {
        testAppointment.setTime(null);

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_MissingService() {
        testAppointment.setService(null);

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_PastDate() {
        testAppointment.setDate(LocalDate.now().minusDays(1).toString());

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointment_InvalidDateFormat() {
        testAppointment.setDate("invalid-date");

        assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.createAppointment(testAppointment, principal);
        });

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testGetAllAppointments_Success() {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(appointmentRepository.findAll()).thenReturn(appointments);

        List<Appointment> result = appointmentService.getAllAppointments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAppointment, result.get(0));
        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    void testGetAppointmentsForUser_Success() {
        List<Appointment> appointments = Arrays.asList(testAppointment);
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(appointmentRepository.findByUser_Id(1L)).thenReturn(appointments);

        List<Appointment> result = appointmentService.getAppointmentsForUser(principal);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAppointment, result.get(0));
        verify(appointmentRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testGetAppointmentsForUser_UserNotFound() {
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            appointmentService.getAppointmentsForUser(principal);
        });

        verify(appointmentRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void testGetAppointmentsForUser_EmptyList() {
        when(userRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testUser));
        when(appointmentRepository.findByUser_Id(1L)).thenReturn(Arrays.asList());

        List<Appointment> result = appointmentService.getAppointmentsForUser(principal);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

