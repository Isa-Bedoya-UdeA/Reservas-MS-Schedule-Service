package com.codefactory.reservasmsscheduleservice.service.impl;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.WorkScheduleResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.WorkSchedule;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.exception.InvalidWorkScheduleException;
import com.codefactory.reservasmsscheduleservice.exception.WorkScheduleConflictException;
import com.codefactory.reservasmsscheduleservice.exception.WorkScheduleNotFoundException;
import com.codefactory.reservasmsscheduleservice.mapper.WorkScheduleMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
import com.codefactory.reservasmsscheduleservice.repository.WorkScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkScheduleServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - WorkScheduleServiceImpl (Unit)")
class WorkScheduleServiceImplTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private WorkScheduleMapper workScheduleMapper;

    @InjectMocks
    private WorkScheduleServiceImpl workScheduleService;

    private UUID employeeId;
    private UUID providerId;
    private UUID scheduleId;
    private Employee employee;
    private WorkSchedule workSchedule;
    private WorkScheduleResponseDTO workScheduleResponseDTO;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        employee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        workSchedule = WorkSchedule.builder()
                .id(scheduleId)
                .employee(employee)
                .dayOfWeek("LUNES")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        workScheduleResponseDTO = WorkScheduleResponseDTO.builder()
                .id(scheduleId)
                .employeeId(employeeId)
                .dayOfWeek("LUNES")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("createWorkSchedule")
    class CreateWorkScheduleTests {

        @Test
        @DisplayName("Debe crear horario laboral exitosamente")
        void createWorkSchedule_ValidRequest_ReturnsSchedule() {
            // Given
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, "LUNES")).thenReturn(List.of());
            when(workScheduleMapper.toEntity(request)).thenReturn(workSchedule);
            when(workScheduleMapper.toDto(any(WorkSchedule.class))).thenReturn(workScheduleResponseDTO);
            when(workScheduleRepository.save(any(WorkSchedule.class))).thenReturn(workSchedule);

            // When
            WorkScheduleResponseDTO result = workScheduleService.createWorkSchedule(request, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDayOfWeek()).isEqualTo("LUNES");
            verify(workScheduleRepository).save(any(WorkSchedule.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void createWorkSchedule_EmployeeNotFound_ThrowsException() {
            // Given
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> workScheduleService.createWorkSchedule(request, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado está inactivo")
        void createWorkSchedule_InactiveEmployee_ThrowsException() {
            // Given
            employee.setActive(false);
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.createWorkSchedule(request, providerId))
                    .isInstanceOf(InvalidWorkScheduleException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si día no es válido")
        void createWorkSchedule_InvalidDay_ThrowsException() {
            // Given
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("INVALIDO")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.createWorkSchedule(request, providerId))
                    .isInstanceOf(InvalidWorkScheduleException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si hora fin es menor o igual a hora inicio")
        void createWorkSchedule_InvalidTimeRange_ThrowsException() {
            // Given
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(18, 0))
                    .endTime(LocalTime.of(9, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.createWorkSchedule(request, providerId))
                    .isInstanceOf(InvalidWorkScheduleException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si hay conflicto de horarios")
        void createWorkSchedule_TimeConflict_ThrowsException() {
            // Given
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, "LUNES"))
                    .thenReturn(List.of(workSchedule));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.createWorkSchedule(request, providerId))
                    .isInstanceOf(WorkScheduleConflictException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño del empleado")
        void createWorkSchedule_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            CreateWorkScheduleRequestDTO request = CreateWorkScheduleRequestDTO.builder()
                    .employeeId(employeeId)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.createWorkSchedule(request, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("updateWorkSchedule")
    class UpdateWorkScheduleTests {

        @Test
        @DisplayName("Debe actualizar horario exitosamente")
        void updateWorkSchedule_ValidRequest_ReturnsUpdated() {
            // Given
            UpdateWorkScheduleRequestDTO request = UpdateWorkScheduleRequestDTO.builder()
                    .dayOfWeek("MARTES")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(19, 0))
                    .build();

            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(workSchedule));
            doNothing().when(workScheduleMapper).updateEntityFromDto(request, workSchedule);
            when(workScheduleMapper.toDto(workSchedule)).thenReturn(workScheduleResponseDTO);
            when(workScheduleRepository.save(workSchedule)).thenReturn(workSchedule);

            // When
            WorkScheduleResponseDTO result = workScheduleService.updateWorkSchedule(scheduleId, request, providerId);

            // Then
            assertThat(result).isNotNull();
            verify(workScheduleRepository).save(workSchedule);
        }

        @Test
        @DisplayName("Debe lanzar excepción si horario no existe")
        void updateWorkSchedule_NotFound_ThrowsException() {
            // Given
            UpdateWorkScheduleRequestDTO request = UpdateWorkScheduleRequestDTO.builder()
                    .dayOfWeek("MARTES")
                    .build();

            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> workScheduleService.updateWorkSchedule(scheduleId, request, providerId))
                    .isInstanceOf(WorkScheduleNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void updateWorkSchedule_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            UpdateWorkScheduleRequestDTO request = UpdateWorkScheduleRequestDTO.builder()
                    .dayOfWeek("MARTES")
                    .build();

            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(workSchedule));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.updateWorkSchedule(scheduleId, request, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deleteWorkSchedule")
    class DeleteWorkScheduleTests {

        @Test
        @DisplayName("Debe eliminar horario exitosamente")
        void deleteWorkSchedule_ValidSchedule_Deletes() {
            // Given
            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(workSchedule));
            doNothing().when(workScheduleRepository).delete(workSchedule);

            // When
            workScheduleService.deleteWorkSchedule(scheduleId, providerId);

            // Then
            verify(workScheduleRepository).delete(workSchedule);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void deleteWorkSchedule_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(workSchedule));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.deleteWorkSchedule(scheduleId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getWorkScheduleById")
    class GetWorkScheduleByIdTests {

        @Test
        @DisplayName("Debe obtener horario por ID")
        void getWorkScheduleById_ExistingSchedule_ReturnsSchedule() {
            // Given
            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(workSchedule));
            when(workScheduleMapper.toDto(workSchedule)).thenReturn(workScheduleResponseDTO);

            // When
            WorkScheduleResponseDTO result = workScheduleService.getWorkScheduleById(scheduleId, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(scheduleId);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no existe")
        void getWorkScheduleById_NotFound_ThrowsException() {
            // Given
            when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> workScheduleService.getWorkScheduleById(scheduleId, providerId))
                    .isInstanceOf(WorkScheduleNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getWorkSchedulesByEmployee")
    class GetWorkSchedulesByEmployeeTests {

        @Test
        @DisplayName("Debe obtener horarios por empleado")
        void getWorkSchedulesByEmployee_ValidEmployee_ReturnsSchedules() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeId(employeeId)).thenReturn(List.of(workSchedule));
            when(workScheduleMapper.toDto(workSchedule)).thenReturn(workScheduleResponseDTO);

            // When
            List<WorkScheduleResponseDTO> result = workScheduleService.getWorkSchedulesByEmployee(employeeId, providerId);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void getWorkSchedulesByEmployee_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> workScheduleService.getWorkSchedulesByEmployee(employeeId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getActiveWorkSchedulesByEmployee")
    class GetActiveWorkSchedulesByEmployeeTests {

        @Test
        @DisplayName("Debe obtener horarios activos")
        void getActiveWorkSchedulesByEmployee_ReturnsActiveSchedules() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findActiveSchedulesByEmployeeOrdered(employeeId))
                    .thenReturn(List.of(workSchedule));
            when(workScheduleMapper.toDto(workSchedule)).thenReturn(workScheduleResponseDTO);

            // When
            List<WorkScheduleResponseDTO> result = workScheduleService.getActiveWorkSchedulesByEmployee(employeeId);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getWorkSchedulesByEmployeePublic")
    class GetWorkSchedulesByEmployeePublicTests {

        @Test
        @DisplayName("Debe obtener horarios activos públicos")
        void getWorkSchedulesByEmployeePublic_ReturnsActiveSchedules() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndActiveTrue(employeeId))
                    .thenReturn(List.of(workSchedule));
            when(workScheduleMapper.toDto(workSchedule)).thenReturn(workScheduleResponseDTO);

            // When
            List<WorkScheduleResponseDTO> result = workScheduleService.getWorkSchedulesByEmployeePublic(employeeId);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}