package com.codefactory.reservasmsscheduleservice.service.impl;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateScheduleBlockRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ScheduleBlockResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.ScheduleBlock;
import com.codefactory.reservasmsscheduleservice.entity.WorkSchedule;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.exception.InvalidScheduleBlockException;
import com.codefactory.reservasmsscheduleservice.exception.ScheduleBlockConflictException;
import com.codefactory.reservasmsscheduleservice.exception.ScheduleBlockNotFoundException;
import com.codefactory.reservasmsscheduleservice.mapper.ScheduleBlockMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
import com.codefactory.reservasmsscheduleservice.repository.ScheduleBlockRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
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
 * Unit tests for ScheduleBlockServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - ScheduleBlockServiceImpl (Unit)")
class ScheduleBlockServiceImplTest {

    @Mock
    private ScheduleBlockRepository scheduleBlockRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ScheduleBlockMapper scheduleBlockMapper;

    @InjectMocks
    private ScheduleBlockServiceImpl scheduleBlockService;

    private UUID employeeId;
    private UUID providerId;
    private UUID blockId;
    private UUID reservationId;
    private Employee employee;
    private ScheduleBlock scheduleBlock;
    private ScheduleBlockResponseDTO scheduleBlockResponseDTO;
    private LocalDate today;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        blockId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        today = LocalDate.now();
        tomorrow = today.plusDays(1);

        employee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        // Calculate the day of week for today
        String dayOfWeek = getDayOfWeekString(today);

        WorkSchedule workSchedule = WorkSchedule.builder()
                .id(UUID.randomUUID())
                .employee(employee)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .active(true)
                .build();

        scheduleBlock = ScheduleBlock.builder()
                .id(blockId)
                .employee(employee)
                .reservationId(reservationId)
                .date(today)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .blockType("RESERVA")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        scheduleBlockResponseDTO = ScheduleBlockResponseDTO.builder()
                .id(blockId)
                .employeeId(employeeId)
                .reservationId(reservationId)
                .date(today)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .blockType("RESERVA")
                .active(true)
                .build();

        // Setup common mocks
        lenient().when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(eq(employeeId), anyString()))
                .thenReturn(List.of(workSchedule));
    }

    private String getDayOfWeekString(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    @Nested
    @DisplayName("createScheduleBlock")
    class CreateScheduleBlockTests {

        @Test
        @DisplayName("Debe crear bloqueo exitosamente")
        void createScheduleBlock_ValidRequest_ReturnsBlock() {
            // Given
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(tomorrow)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .blockType("RESERVA")
                    .build();

            String dayOfWeek = getDayOfWeekString(tomorrow);

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of(WorkSchedule.builder()
                            .employee(employee)
                            .dayOfWeek(dayOfWeek)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(18, 0))
                            .active(true)
                            .build()));
            when(scheduleBlockRepository.findOverlappingBlocks(eq(employeeId), eq(tomorrow), any(), any()))
                    .thenReturn(List.of());
            when(scheduleBlockMapper.toEntity(request)).thenReturn(scheduleBlock);
            when(scheduleBlockMapper.toDto(any(ScheduleBlock.class))).thenReturn(scheduleBlockResponseDTO);
            when(scheduleBlockRepository.save(any(ScheduleBlock.class))).thenReturn(scheduleBlock);

            // When
            ScheduleBlockResponseDTO result = scheduleBlockService.createScheduleBlock(request, providerId);

            // Then
            assertThat(result).isNotNull();
            verify(scheduleBlockRepository).save(any(ScheduleBlock.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void createScheduleBlock_EmployeeNotFound_ThrowsException() {
            // Given
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(tomorrow)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createScheduleBlock(request, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void createScheduleBlock_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(tomorrow)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createScheduleBlock(request, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si tipo de bloqueo es inválido")
        void createScheduleBlock_InvalidBlockType_ThrowsException() {
            // Given
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(tomorrow)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .blockType("INVALIDO")
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createScheduleBlock(request, providerId))
                    .isInstanceOf(InvalidScheduleBlockException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si fecha es en el pasado")
        void createScheduleBlock_PastDate_ThrowsException() {
            // Given
            LocalDate pastDate = today.minusDays(1);
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(pastDate)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createScheduleBlock(request, providerId))
                    .isInstanceOf(InvalidScheduleBlockException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si hora fin es menor o igual a hora inicio")
        void createScheduleBlock_InvalidTimeRange_ThrowsException() {
            // Given
            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(tomorrow)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(10, 0))
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createScheduleBlock(request, providerId))
                    .isInstanceOf(InvalidScheduleBlockException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si hay conflicto de bloqueos")
        void createScheduleBlock_TimeConflict_ThrowsException() {
            // Given
            String dayOfWeek = getDayOfWeekString(tomorrow);

            CreateScheduleBlockRequestDTO request = CreateScheduleBlockRequestDTO.builder()
                    .employeeId(employeeId)
                    .date(tomorrow)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .blockType("RESERVA")
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of(WorkSchedule.builder()
                            .employee(employee)
                            .dayOfWeek(dayOfWeek)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(18, 0))
                            .active(true)
                            .build()));
            when(scheduleBlockRepository.findOverlappingBlocks(eq(employeeId), eq(tomorrow), any(), any()))
                    .thenReturn(List.of(scheduleBlock));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createScheduleBlock(request, providerId))
                    .isInstanceOf(ScheduleBlockConflictException.class);
        }
    }

    @Nested
    @DisplayName("deleteScheduleBlock")
    class DeleteScheduleBlockTests {

        @Test
        @DisplayName("Debe eliminar (soft delete) bloqueo exitosamente")
        void deleteScheduleBlock_ValidBlock_SoftDeletes() {
            // Given
            when(scheduleBlockRepository.findById(blockId)).thenReturn(Optional.of(scheduleBlock));
            when(scheduleBlockRepository.save(any(ScheduleBlock.class))).thenReturn(scheduleBlock);

            // When
            scheduleBlockService.deleteScheduleBlock(blockId, providerId);

            // Then
            assertThat(scheduleBlock.getActive()).isFalse();
            verify(scheduleBlockRepository).save(scheduleBlock);
        }

        @Test
        @DisplayName("Debe lanzar excepción si bloqueo no existe")
        void deleteScheduleBlock_NotFound_ThrowsException() {
            // Given
            when(scheduleBlockRepository.findById(blockId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.deleteScheduleBlock(blockId, providerId))
                    .isInstanceOf(ScheduleBlockNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void deleteScheduleBlock_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(scheduleBlockRepository.findById(blockId)).thenReturn(Optional.of(scheduleBlock));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.deleteScheduleBlock(blockId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getScheduleBlockById")
    class GetScheduleBlockByIdTests {

        @Test
        @DisplayName("Debe obtener bloqueo por ID")
        void getScheduleBlockById_ExistingBlock_ReturnsBlock() {
            // Given
            when(scheduleBlockRepository.findById(blockId)).thenReturn(Optional.of(scheduleBlock));
            when(scheduleBlockMapper.toDto(scheduleBlock)).thenReturn(scheduleBlockResponseDTO);

            // When
            ScheduleBlockResponseDTO result = scheduleBlockService.getScheduleBlockById(blockId, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(blockId);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no existe")
        void getScheduleBlockById_NotFound_ThrowsException() {
            // Given
            when(scheduleBlockRepository.findById(blockId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.getScheduleBlockById(blockId, providerId))
                    .isInstanceOf(ScheduleBlockNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void getScheduleBlockById_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(scheduleBlockRepository.findById(blockId)).thenReturn(Optional.of(scheduleBlock));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.getScheduleBlockById(blockId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getScheduleBlocksByEmployee")
    class GetScheduleBlocksByEmployeeTests {

        @Test
        @DisplayName("Debe obtener bloqueos por empleado")
        void getScheduleBlocksByEmployee_ValidEmployee_ReturnsBlocks() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndActiveTrue(employeeId))
                    .thenReturn(List.of(scheduleBlock));
            when(scheduleBlockMapper.toDto(scheduleBlock)).thenReturn(scheduleBlockResponseDTO);

            // When
            List<ScheduleBlockResponseDTO> result = scheduleBlockService.getScheduleBlocksByEmployee(employeeId, providerId);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void getScheduleBlocksByEmployee_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.getScheduleBlocksByEmployee(employeeId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("createReservationBlock")
    class CreateReservationBlockTests {

        @Test
        @DisplayName("Debe crear bloqueo de reserva exitosamente")
        void createReservationBlock_ValidRequest_CreatesBlock() {
            // Given
            String dayOfWeek = getDayOfWeekString(today);

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of(WorkSchedule.builder()
                            .employee(employee)
                            .dayOfWeek(dayOfWeek)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(18, 0))
                            .active(true)
                            .build()));
            when(scheduleBlockRepository.findOverlappingBlocks(eq(employeeId), eq(today), any(), any()))
                    .thenReturn(List.of());
            when(scheduleBlockRepository.save(any(ScheduleBlock.class))).thenReturn(scheduleBlock);

            // When
            scheduleBlockService.createReservationBlock(
                    employeeId, reservationId, today,
                    LocalTime.of(10, 0), LocalTime.of(11, 0));

            // Then
            verify(scheduleBlockRepository).save(any(ScheduleBlock.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si hay conflicto")
        void createReservationBlock_Conflict_ThrowsException() {
            // Given
            String dayOfWeek = getDayOfWeekString(today);

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of(WorkSchedule.builder()
                            .employee(employee)
                            .dayOfWeek(dayOfWeek)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(18, 0))
                            .active(true)
                            .build()));
            when(scheduleBlockRepository.findOverlappingBlocks(eq(employeeId), eq(today), any(), any()))
                    .thenReturn(List.of(scheduleBlock));

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.createReservationBlock(
                    employeeId, reservationId, today,
                    LocalTime.of(10, 0), LocalTime.of(11, 0)))
                    .isInstanceOf(ScheduleBlockConflictException.class);
        }
    }

    @Nested
    @DisplayName("cancelReservationBlock")
    class CancelReservationBlockTests {

        @Test
        @DisplayName("Debe cancelar bloqueo de reserva exitosamente")
        void cancelReservationBlock_ExistingReservation_Cancels() {
            // Given
            when(scheduleBlockRepository.findActiveByReservationId(reservationId))
                    .thenReturn(Optional.of(scheduleBlock));
            when(scheduleBlockRepository.save(any(ScheduleBlock.class))).thenReturn(scheduleBlock);

            // When
            scheduleBlockService.cancelReservationBlock(reservationId);

            // Then
            assertThat(scheduleBlock.getActive()).isFalse();
            verify(scheduleBlockRepository).save(scheduleBlock);
        }

        @Test
        @DisplayName("Debe lanzar excepción si reserva no existe")
        void cancelReservationBlock_NotFound_ThrowsException() {
            // Given
            when(scheduleBlockRepository.findActiveByReservationId(reservationId))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> scheduleBlockService.cancelReservationBlock(reservationId))
                    .isInstanceOf(ScheduleBlockNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("isEmployeeAvailable")
    class IsEmployeeAvailableTests {

        @Test
        @DisplayName("Debe retornar true si empleado está disponible")
        void isEmployeeAvailable_NoConflicts_ReturnsTrue() {
            // Given
            String dayOfWeek = getDayOfWeekString(today);

            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of(WorkSchedule.builder()
                            .employee(employee)
                            .dayOfWeek(dayOfWeek)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(18, 0))
                            .active(true)
                            .build()));
            when(scheduleBlockRepository.findOverlappingBlocks(eq(employeeId), eq(today), any(), any()))
                    .thenReturn(List.of());

            // When
            boolean result = scheduleBlockService.isEmployeeAvailable(
                    employeeId, today, LocalTime.of(10, 0), LocalTime.of(11, 0));

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si hay conflicto")
        void isEmployeeAvailable_HasConflict_ReturnsFalse() {
            // Given
            String dayOfWeek = getDayOfWeekString(today);

            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of(WorkSchedule.builder()
                            .employee(employee)
                            .dayOfWeek(dayOfWeek)
                            .startTime(LocalTime.of(9, 0))
                            .endTime(LocalTime.of(18, 0))
                            .active(true)
                            .build()));
            when(scheduleBlockRepository.findOverlappingBlocks(eq(employeeId), eq(today), any(), any()))
                    .thenReturn(List.of(scheduleBlock));

            // When
            boolean result = scheduleBlockService.isEmployeeAvailable(
                    employeeId, today, LocalTime.of(10, 0), LocalTime.of(11, 0));

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false si empleado no trabaja ese día")
        void isEmployeeAvailable_NotWorking_ReturnsFalse() {
            // Given
            String dayOfWeek = getDayOfWeekString(today);

            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employeeId, dayOfWeek))
                    .thenReturn(List.of());

            // When
            boolean result = scheduleBlockService.isEmployeeAvailable(
                    employeeId, today, LocalTime.of(10, 0), LocalTime.of(11, 0));

            // Then
            assertThat(result).isFalse();
        }
    }
}