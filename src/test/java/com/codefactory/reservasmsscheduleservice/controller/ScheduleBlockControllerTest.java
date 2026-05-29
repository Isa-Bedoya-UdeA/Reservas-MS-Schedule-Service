package com.codefactory.reservasmsscheduleservice.controller;

import com.codefactory.reservasmsscheduleservice.dto.request.CheckAvailabilityRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.CreateReservationBlockRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.CreateScheduleBlockRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.GetScheduleBlocksByDateRangeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ScheduleBlockResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.ScheduleBlockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleBlockController using @ExtendWith(MockitoExtension).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - ScheduleBlockController (Unit)")
class ScheduleBlockControllerTest {

    @Mock
    private ScheduleBlockService scheduleBlockService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ScheduleBlockController scheduleBlockController;

    private UUID employeeId;
    private UUID blockId;
    private UUID reservationId;
    private UUID providerId;
    private LocalDate today;
    private ScheduleBlockResponseDTO scheduleBlockResponse;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        blockId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        today = LocalDate.now();

        scheduleBlockResponse = ScheduleBlockResponseDTO.builder()
                .id(blockId)
                .employeeId(employeeId)
                .reservationId(reservationId)
                .date(today)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .blockType("RESERVA")
                .active(true)
                .build();

        // Setup security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(providerId.toString());
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("createScheduleBlock")
    class CreateScheduleBlockTests {

        @Test
        @DisplayName("Debe crear bloqueo y retornar CREATED")
        void createScheduleBlock_ReturnsCreated() {
            // Given
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(today.plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .blockType("RESERVA")
                    .build();

            when(scheduleBlockService.createScheduleBlock(any(), eq(providerId))).thenReturn(scheduleBlockResponse);

            // When
            ResponseEntity<ScheduleBlockResponseDTO> response = scheduleBlockController.createScheduleBlock(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("deleteScheduleBlock")
    class DeleteScheduleBlockTests {

        @Test
        @DisplayName("Debe eliminar bloqueo y retornar NO_CONTENT")
        void deleteScheduleBlock_ReturnsNoContent() {
            // Given
            doNothing().when(scheduleBlockService).deleteScheduleBlock(blockId, providerId);

            // When
            ResponseEntity<Void> response = scheduleBlockController.deleteScheduleBlock(blockId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(scheduleBlockService).deleteScheduleBlock(blockId, providerId);
        }
    }

    @Nested
    @DisplayName("getScheduleBlockById")
    class GetScheduleBlockByIdTests {

        @Test
        @DisplayName("Debe obtener bloqueo por ID y retornar OK")
        void getScheduleBlockById_ReturnsOk() {
            // Given
            when(scheduleBlockService.getScheduleBlockById(blockId, providerId)).thenReturn(scheduleBlockResponse);

            // When
            ResponseEntity<ScheduleBlockResponseDTO> response = scheduleBlockController.getScheduleBlockById(blockId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(blockId);
        }
    }

    @Nested
    @DisplayName("getScheduleBlocksByEmployee")
    class GetScheduleBlocksByEmployeeTests {

        @Test
        @DisplayName("Debe obtener bloqueos por empleado y retornar OK")
        void getScheduleBlocksByEmployee_ReturnsOk() {
            // Given
            when(scheduleBlockService.getScheduleBlocksByEmployee(employeeId, providerId))
                    .thenReturn(List.of(scheduleBlockResponse));

            // When
            ResponseEntity<List<ScheduleBlockResponseDTO>> response = scheduleBlockController.getScheduleBlocksByEmployee(employeeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getScheduleBlocksByEmployeePublic")
    class GetScheduleBlocksByEmployeePublicTests {

        @Test
        @DisplayName("Debe obtener bloqueos públicos y retornar OK")
        void getScheduleBlocksByEmployeePublic_ReturnsOk() {
            // Given
            when(scheduleBlockService.getScheduleBlocksByEmployeePublic(employeeId))
                    .thenReturn(List.of(scheduleBlockResponse));

            // When
            ResponseEntity<List<ScheduleBlockResponseDTO>> response = scheduleBlockController.getScheduleBlocksByEmployeePublic(employeeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getScheduleBlocksByEmployeeAndDateRange")
    class GetScheduleBlocksByEmployeeAndDateRangeTests {

        @Test
        @DisplayName("Debe obtener bloqueos por rango de fechas y retornar OK")
        void getScheduleBlocksByEmployeeAndDateRange_ReturnsOk() {
            // Given
            GetScheduleBlocksByDateRangeRequestDTO request = GetScheduleBlocksByDateRangeRequestDTO.builder()
                    .employeeId(employeeId)
                    .startDate(today)
                    .endDate(today.plusDays(7))
                    .build();

            when(scheduleBlockService.getScheduleBlocksByEmployeeAndDateRange(
                    eq(employeeId), eq(today), eq(today.plusDays(7))))
                    .thenReturn(List.of(scheduleBlockResponse));

            // When
            ResponseEntity<List<ScheduleBlockResponseDTO>> response = scheduleBlockController
                    .getScheduleBlocksByEmployeeAndDateRange(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getScheduleBlocksByEmployeeAndDate")
    class GetScheduleBlocksByEmployeeAndDateTests {

        @Test
        @DisplayName("Debe obtener bloqueos por fecha y retornar OK")
        void getScheduleBlocksByEmployeeAndDate_ReturnsOk() {
            // Given
            when(scheduleBlockService.getScheduleBlocksByEmployeeAndDate(employeeId, today))
                    .thenReturn(List.of(scheduleBlockResponse));

            // When
            ResponseEntity<List<ScheduleBlockResponseDTO>> response = scheduleBlockController
                    .getScheduleBlocksByEmployeeAndDate(employeeId, today);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("createReservationBlock")
    class CreateReservationBlockTests {

        @Test
        @DisplayName("Debe crear bloqueo de reserva y retornar CREATED")
        void createReservationBlock_ReturnsCreated() {
            // Given
            CreateReservationBlockRequestDTO request = CreateReservationBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .reservationId(reservationId)
                    .date(today)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            doNothing().when(scheduleBlockService).createReservationBlock(
                    eq(employeeId), eq(reservationId), eq(today), any(), any());

            // When
            ResponseEntity<Void> response = scheduleBlockController.createReservationBlock(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(scheduleBlockService).createReservationBlock(
                    eq(employeeId), eq(reservationId), eq(today), any(), any());
        }
    }

    @Nested
    @DisplayName("cancelReservationBlock")
    class CancelReservationBlockTests {

        @Test
        @DisplayName("Debe cancelar reserva y retornar NO_CONTENT")
        void cancelReservationBlock_ReturnsNoContent() {
            // Given
            doNothing().when(scheduleBlockService).cancelReservationBlock(reservationId);

            // When
            ResponseEntity<Void> response = scheduleBlockController.cancelReservationBlock(reservationId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(scheduleBlockService).cancelReservationBlock(reservationId);
        }
    }

    @Nested
    @DisplayName("checkEmployeeAvailability")
    class CheckEmployeeAvailabilityTests {

        @Test
        @DisplayName("Debe verificar disponibilidad y retornar OK con true")
        void checkEmployeeAvailability_ReturnsTrue() {
            // Given
            CheckAvailabilityRequestDTO request = CheckAvailabilityRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(today)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            when(scheduleBlockService.isEmployeeAvailable(
                    eq(employeeId), eq(today), any(), any()))
                    .thenReturn(true);

            // When
            ResponseEntity<Boolean> response = scheduleBlockController.checkEmployeeAvailability(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
        }

        @Test
        @DisplayName("Debe verificar disponibilidad y retornar OK con false")
        void checkEmployeeAvailability_ReturnsFalse() {
            // Given
            CheckAvailabilityRequestDTO request = CheckAvailabilityRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(today)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            when(scheduleBlockService.isEmployeeAvailable(
                    eq(employeeId), eq(today), any(), any()))
                    .thenReturn(false);

            // When
            ResponseEntity<Boolean> response = scheduleBlockController.checkEmployeeAvailability(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
        }
    }
}