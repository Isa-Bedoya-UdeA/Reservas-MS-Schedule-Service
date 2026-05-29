package com.codefactory.reservasmsscheduleservice.repository;

import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.WorkSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para WorkScheduleRepository.
 * Utiliza @DataJpaTest con H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class WorkScheduleRepositoryTest {

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee employee;

    @BeforeEach
    void setUp() {
        // Create and persist an employee first
        employee = Employee.builder()
                .providerId(UUID.randomUUID())
                .fullName("Test Employee")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();
        employee = entityManager.persist(employee);
        entityManager.flush();
    }

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar horario laboral con todos los campos")
        void save_NewWorkSchedule_SavesAllFields() {
            // Given
            WorkSchedule schedule = WorkSchedule.builder()
                    .employee(employee)
                    .dayOfWeek("LUNES")
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .active(true)
                    .build();

            // When
            WorkSchedule saved = workScheduleRepository.save(schedule);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDayOfWeek()).isEqualTo("LUNES");
            assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(saved.getActive()).isTrue();
        }

        @Test
        @DisplayName("Debe guardar horario inactivo por defecto")
        void save_ScheduleInactive_DefaultsToInactive() {
            // Given
            WorkSchedule schedule = WorkSchedule.builder()
                    .employee(employee)
                    .dayOfWeek("MARTES")
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(19, 0))
                    .active(false)
                    .build();

            // When
            WorkSchedule saved = workScheduleRepository.save(schedule);
            entityManager.flush();

            // Then
            assertThat(saved.getActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar horario por ID")
        void findById_ExistingSchedule_ReturnsSchedule() {
            // Given
            WorkSchedule schedule = createAndPersistSchedule("MIERCOLES", LocalTime.of(8, 0), LocalTime.of(17, 0), true);
            UUID scheduleId = schedule.getId();

            // When
            Optional<WorkSchedule> found = workScheduleRepository.findById(scheduleId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getDayOfWeek()).isEqualTo("MIERCOLES");
        }

        @Test
        @DisplayName("Debe retornar empty para ID no existente")
        void findById_NonExistingId_ReturnsEmpty() {
            // When
            Optional<WorkSchedule> found = workScheduleRepository.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmployeeId")
    class FindByEmployeeIdTests {

        @Test
        @DisplayName("Debe retornar horarios de un empleado")
        void findByEmployeeId_ExistingEmployee_ReturnsSchedules() {
            // Given
            createAndPersistSchedule("LUNES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);
            createAndPersistSchedule("MARTES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);
            createAndPersistSchedule("MIERCOLES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);

            // When
            List<WorkSchedule> schedules = workScheduleRepository.findByEmployeeId(employee.getId());

            // Then
            assertThat(schedules).hasSize(3);
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndDayOfWeekAndActiveTrue")
    class FindByEmployeeIdAndDayOfWeekAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar horario activo por día de semana")
        void findByEmployeeIdAndDayOfWeekAndActiveTrue_ReturnsActiveSchedule() {
            // Given
            createAndPersistSchedule("JUEVES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);
            createAndPersistSchedule("JUEVES", LocalTime.of(14, 0), LocalTime.of(18, 0), false);

            // When
            List<WorkSchedule> found = workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employee.getId(), "JUEVES");

            // Then
            assertThat(found).hasSize(1);
            assertThat(found.get(0).getDayOfWeek()).isEqualTo("JUEVES");
            assertThat(found.get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay horario activo para ese día")
        void findByEmployeeIdAndDayOfWeekAndActiveTrue_NoActiveSchedule_ReturnsEmpty() {
            // When
            List<WorkSchedule> found = workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(employee.getId(), "DOMINGO");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndActiveTrue")
    class FindByEmployeeIdAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar solo horarios activos")
        void findByEmployeeIdAndActiveTrue_ReturnsOnlyActive() {
            // Given
            createAndPersistSchedule("VIERNES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);
            createAndPersistSchedule("SABADO", LocalTime.of(10, 0), LocalTime.of(15, 0), false);

            // When
            List<WorkSchedule> activeSchedules = workScheduleRepository.findByEmployeeIdAndActiveTrue(employee.getId());

            // Then
            assertThat(activeSchedules).hasSize(1);
            assertThat(activeSchedules.get(0).getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("existsByIdAndEmployeeId")
    class ExistsByIdAndEmployeeIdTests {

        @Test
        @DisplayName("Debe retornar true para combinación existente")
        void existsByIdAndEmployeeId_Existing_ReturnsTrue() {
            // Given
            WorkSchedule schedule = createAndPersistSchedule("LUNES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);

            // When
            boolean exists = workScheduleRepository.existsByIdAndEmployeeId(schedule.getId(), employee.getId());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false para combinación no existente")
        void existsByIdAndEmployeeId_NonExisting_ReturnsFalse() {
            // When
            boolean exists = workScheduleRepository.existsByIdAndEmployeeId(UUID.randomUUID(), employee.getId());

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar horario por ID")
        void deleteById_ExistingSchedule_DeletesSchedule() {
            // Given
            WorkSchedule schedule = createAndPersistSchedule("LUNES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);
            UUID scheduleId = schedule.getId();

            // When
            workScheduleRepository.deleteById(scheduleId);
            entityManager.flush();

            // Then
            Optional<WorkSchedule> found = workScheduleRepository.findById(scheduleId);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndEmployeeId")
    class FindByIdAndEmployeeIdTests {

        @Test
        @DisplayName("Debe encontrar horario por ID y empleado")
        void findByIdAndEmployeeId_Existing_ReturnsSchedule() {
            // Given
            WorkSchedule schedule = createAndPersistSchedule("LUNES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);

            // When
            Optional<WorkSchedule> found = workScheduleRepository.findByIdAndEmployeeId(schedule.getId(), employee.getId());

            // Then
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Debe retornar empty si empleado no coincide")
        void findByIdAndEmployeeId_WrongEmployee_ReturnsEmpty() {
            // Given
            WorkSchedule schedule = createAndPersistSchedule("LUNES", LocalTime.of(9, 0), LocalTime.of(18, 0), true);

            // When
            Optional<WorkSchedule> found = workScheduleRepository.findByIdAndEmployeeId(schedule.getId(), UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    // Helper methods
    private WorkSchedule createAndPersistSchedule(String dayOfWeek, LocalTime start, LocalTime end, boolean active) {
        WorkSchedule schedule = WorkSchedule.builder()
                .employee(employee)
                .dayOfWeek(dayOfWeek)
                .startTime(start)
                .endTime(end)
                .active(active)
                .build();
        return entityManager.persist(schedule);
    }
}