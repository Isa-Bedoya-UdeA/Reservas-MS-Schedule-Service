package com.codefactory.reservasmsscheduleservice.controller;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.WorkScheduleResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.WorkScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkScheduleController using @ExtendWith(MockitoExtension).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - WorkScheduleController (Unit)")
class WorkScheduleControllerTest {

    @Mock
    private WorkScheduleService workScheduleService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private WorkScheduleController workScheduleController;

    private UUID employeeId;
    private UUID scheduleId;
    private UUID providerId;
    private WorkScheduleResponseDTO workScheduleResponse;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        workScheduleResponse = WorkScheduleResponseDTO.builder()
                .id(scheduleId)
                .employeeId(employeeId)
                .dayOfWeek("LUNES")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .active(true)
                .build();

        // Setup security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(providerId.toString());
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("createWorkSchedule")
    class CreateWorkScheduleTests {

        @Test
        @DisplayName("Debe crear horario y retornar CREATED")
        void createWorkSchedule_ReturnsCreated() {
            // Given
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(workScheduleService.createWorkSchedule(any(), eq(providerId))).thenReturn(workScheduleResponse);

            // When
            ResponseEntity<EntityModel<WorkScheduleResponseDTO>> response = workScheduleController.createWorkSchedule(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().getDayOfWeek()).isEqualTo("LUNES");
        }
    }

    @Nested
    @DisplayName("updateWorkSchedule")
    class UpdateWorkScheduleTests {

        @Test
        @DisplayName("Debe actualizar horario y retornar OK")
        void updateWorkSchedule_ReturnsOk() {
            // Given
            UpdateWorkScheduleRequestDTO request = UpdateWorkScheduleRequestDTO.builder()
                    .dayOfWeek("MARTES")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(19, 0))
                    .build();

            WorkScheduleResponseDTO updated = WorkScheduleResponseDTO.builder()
                    .id(scheduleId)
                    .employeeId(employeeId)
                    .dayOfWeek("MARTES")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(19, 0))
                    .active(true)
                    .build();

            when(workScheduleService.updateWorkSchedule(eq(scheduleId), any(), eq(providerId))).thenReturn(updated);

            // When
            ResponseEntity<EntityModel<WorkScheduleResponseDTO>> response = workScheduleController.updateWorkSchedule(scheduleId, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().getDayOfWeek()).isEqualTo("MARTES");
        }
    }

    @Nested
    @DisplayName("deleteWorkSchedule")
    class DeleteWorkScheduleTests {

        @Test
        @DisplayName("Debe eliminar horario y retornar NO_CONTENT")
        void deleteWorkSchedule_ReturnsNoContent() {
            // Given
            doNothing().when(workScheduleService).deleteWorkSchedule(scheduleId, providerId);

            // When
            ResponseEntity<Void> response = workScheduleController.deleteWorkSchedule(scheduleId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(workScheduleService).deleteWorkSchedule(scheduleId, providerId);
        }
    }

    @Nested
    @DisplayName("getWorkScheduleById")
    class GetWorkScheduleByIdTests {

        @Test
        @DisplayName("Debe obtener horario por ID y retornar OK")
        void getWorkScheduleById_ReturnsOk() {
            // Given
            when(workScheduleService.getWorkScheduleById(scheduleId, providerId)).thenReturn(workScheduleResponse);

            // When
            ResponseEntity<EntityModel<WorkScheduleResponseDTO>> response = workScheduleController.getWorkScheduleById(scheduleId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().getId()).isEqualTo(scheduleId);
        }
    }

    @Nested
    @DisplayName("getWorkSchedulesByEmployee")
    class GetWorkSchedulesByEmployeeTests {

        @Test
        @DisplayName("Debe obtener horarios por empleado y retornar OK")
        void getWorkSchedulesByEmployee_ReturnsOk() {
            // Given
            when(workScheduleService.getWorkSchedulesByEmployee(employeeId, providerId))
                    .thenReturn(List.of(workScheduleResponse));

            // When
            ResponseEntity<CollectionModel<EntityModel<WorkScheduleResponseDTO>>> response = workScheduleController.getWorkSchedulesByEmployee(employeeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getActiveWorkSchedulesByEmployee")
    class GetActiveWorkSchedulesByEmployeeTests {

        @Test
        @DisplayName("Debe obtener horarios activos y retornar OK")
        void getActiveWorkSchedulesByEmployee_ReturnsOk() {
            // Given
            when(workScheduleService.getActiveWorkSchedulesByEmployee(employeeId))
                    .thenReturn(List.of(workScheduleResponse));

            // When
            ResponseEntity<CollectionModel<EntityModel<WorkScheduleResponseDTO>>> response = workScheduleController.getActiveWorkSchedulesByEmployee(employeeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getWorkSchedulesByEmployeePublic")
    class GetWorkSchedulesByEmployeePublicTests {

        @Test
        @DisplayName("Debe obtener horarios públicos y retornar OK")
        void getWorkSchedulesByEmployeePublic_ReturnsOk() {
            // Given
            when(workScheduleService.getWorkSchedulesByEmployeePublic(employeeId))
                    .thenReturn(List.of(workScheduleResponse));

            // When
            ResponseEntity<CollectionModel<EntityModel<WorkScheduleResponseDTO>>> response = workScheduleController.getWorkSchedulesByEmployeePublic(employeeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }
}