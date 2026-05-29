package com.codefactory.reservasmsscheduleservice.repository;

import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.ScheduleBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para ScheduleBlockRepository.
 * Utiliza @DataJpaTest con H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ScheduleBlockRepositoryTest {

    @Autowired
    private ScheduleBlockRepository scheduleBlockRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

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
        @DisplayName("Debe guardar bloque horario con todos los campos")
        void save_NewScheduleBlock_SavesAllFields() {
            // Given
            LocalDate today = LocalDate.now();
            ScheduleBlock block = ScheduleBlock.builder()
                    .employee(employee)
                    .date(today)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .blockType("RESERVA")
                    .active(true)
                    .build();

            // When
            ScheduleBlock saved = scheduleBlockRepository.save(block);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getDate()).isEqualTo(today);
            assertThat(saved.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(saved.getEndTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(saved.getActive()).isTrue();
        }

        @Test
        @DisplayName("Debe guardar bloque inactivo por defecto")
        void save_BlockInactive_DefaultsToInactive() {
            // Given
            ScheduleBlock block = ScheduleBlock.builder()
                    .employee(employee)
                    .date(LocalDate.now())
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 0))
                    .active(false)
                    .build();

            // When
            ScheduleBlock saved = scheduleBlockRepository.save(block);
            entityManager.flush();

            // Then
            assertThat(saved.getActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar bloque por ID")
        void findById_ExistingBlock_ReturnsBlock() {
            // Given
            ScheduleBlock block = createAndPersistBlock(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), true);
            UUID blockId = block.getId();

            // When
            Optional<ScheduleBlock> found = scheduleBlockRepository.findById(blockId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Debe retornar empty para ID no existente")
        void findById_NonExistingId_ReturnsEmpty() {
            // When
            Optional<ScheduleBlock> found = scheduleBlockRepository.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmployeeId")
    class FindByEmployeeIdTests {

        @Test
        @DisplayName("Debe retornar bloques de un empleado")
        void findByEmployeeId_ExistingEmployee_ReturnsBlocks() {
            // Given
            createAndPersistBlock(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            createAndPersistBlock(LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(15, 0), true);

            // When
            List<ScheduleBlock> blocks = scheduleBlockRepository.findByEmployeeId(employee.getId());

            // Then
            assertThat(blocks).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndActiveTrue")
    class FindByEmployeeIdAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar solo bloques activos")
        void findByEmployeeIdAndActiveTrue_ReturnsOnlyActive() {
            // Given
            createAndPersistBlock(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            createAndPersistBlock(LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), false);

            // When
            List<ScheduleBlock> activeBlocks = scheduleBlockRepository.findByEmployeeIdAndActiveTrue(employee.getId());

            // Then
            assertThat(activeBlocks).hasSize(1);
            assertThat(activeBlocks.get(0).getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndDateAndActiveTrue")
    class FindByEmployeeIdAndDateAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar bloques activos de un empleado en fecha específica")
        void findByEmployeeIdAndDateAndActiveTrue_ReturnsBlocksForDate() {
            // Given
            LocalDate today = LocalDate.now();
            createAndPersistBlock(today, LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            createAndPersistBlock(today, LocalTime.of(10, 0), LocalTime.of(11, 0), true);
            createAndPersistBlock(today.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), true);

            // When
            List<ScheduleBlock> blocks = scheduleBlockRepository.findByEmployeeIdAndDateAndActiveTrue(employee.getId(), today);

            // Then
            assertThat(blocks).hasSize(2);
            assertThat(blocks).allMatch(b -> b.getDate().equals(today));
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndDateBetweenAndActiveTrue")
    class FindByEmployeeIdAndDateBetweenAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar bloques activos en rango de fechas")
        void findByEmployeeIdAndDateBetweenAndActiveTrue_ReturnsBlocksInRange() {
            // Given
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(3);
            createAndPersistBlock(startDate, LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            createAndPersistBlock(startDate.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            createAndPersistBlock(startDate.plusDays(2), LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            createAndPersistBlock(endDate.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0), true);

            // When
            List<ScheduleBlock> blocks = scheduleBlockRepository
                    .findByEmployeeIdAndDateBetweenAndActiveTrue(employee.getId(), startDate, endDate);

            // Then
            assertThat(blocks).hasSize(3);
        }
    }

    @Nested
    @DisplayName("existsByIdAndEmployeeId")
    class ExistsByIdAndEmployeeIdTests {

        @Test
        @DisplayName("Debe retornar true para combinación existente")
        void existsByIdAndEmployeeId_Existing_ReturnsTrue() {
            // Given
            ScheduleBlock block = createAndPersistBlock(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), true);

            // When
            boolean exists = scheduleBlockRepository.existsByIdAndEmployeeId(block.getId(), employee.getId());

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false para combinación no existente")
        void existsByIdAndEmployeeId_NonExisting_ReturnsFalse() {
            // When
            boolean exists = scheduleBlockRepository.existsByIdAndEmployeeId(UUID.randomUUID(), employee.getId());

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar bloque por ID")
        void deleteById_ExistingBlock_DeletesBlock() {
            // Given
            ScheduleBlock block = createAndPersistBlock(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), true);
            UUID blockId = block.getId();

            // When
            scheduleBlockRepository.deleteById(blockId);
            entityManager.flush();

            // Then
            Optional<ScheduleBlock> found = scheduleBlockRepository.findById(blockId);
            assertThat(found).isEmpty();
        }
    }

    // Helper methods
    private ScheduleBlock createAndPersistBlock(LocalDate date, LocalTime start, LocalTime end, boolean active) {
        ScheduleBlock block = ScheduleBlock.builder()
                .employee(employee)
                .date(date)
                .startTime(start)
                .endTime(end)
                .blockType("RESERVA")
                .active(active)
                .build();
        return entityManager.persist(block);
    }
}