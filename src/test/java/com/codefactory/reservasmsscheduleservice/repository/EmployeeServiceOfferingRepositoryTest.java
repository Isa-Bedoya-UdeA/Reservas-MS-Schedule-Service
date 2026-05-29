package com.codefactory.reservasmsscheduleservice.repository;

import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.EmployeeServiceOffering;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para EmployeeServiceOfferingRepository.
 * Utiliza @DataJpaTest con H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class EmployeeServiceOfferingRepositoryTest {

    @Autowired
    private EmployeeServiceOfferingRepository employeeServiceOfferingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Employee employee;
    private UUID serviceId;

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
        
        serviceId = UUID.randomUUID();
        entityManager.flush();
    }

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar relación empleado-servicio")
        void save_NewEmployeeServiceOffering_SavesRelation() {
            // Given
            EmployeeServiceOffering eso = EmployeeServiceOffering.builder()
                    .employee(employee)
                    .serviceId(serviceId)
                    .active(true)
                    .build();

            // When
            EmployeeServiceOffering saved = employeeServiceOfferingRepository.save(eso);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getServiceId()).isEqualTo(serviceId);
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getAssignmentDate()).isNotNull();
        }

        @Test
        @DisplayName("Debe guardar relación inactiva por defecto")
        void save_RelationInactive_DefaultsToInactive() {
            // Given
            EmployeeServiceOffering eso = EmployeeServiceOffering.builder()
                    .employee(employee)
                    .serviceId(serviceId)
                    .active(false)
                    .build();

            // When
            EmployeeServiceOffering saved = employeeServiceOfferingRepository.save(eso);
            entityManager.flush();

            // Then
            assertThat(saved.getActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar relación por ID")
        void findById_ExistingRelation_ReturnsRelation() {
            // Given
            EmployeeServiceOffering eso = createAndPersistRelation(serviceId, true);
            UUID esoId = eso.getId();

            // When
            Optional<EmployeeServiceOffering> found = employeeServiceOfferingRepository.findById(esoId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getServiceId()).isEqualTo(serviceId);
        }

        @Test
        @DisplayName("Debe retornar empty para ID no existente")
        void findById_NonExistingId_ReturnsEmpty() {
            // When
            Optional<EmployeeServiceOffering> found = employeeServiceOfferingRepository.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmployeeId")
    class FindByEmployeeIdTests {

        @Test
        @DisplayName("Debe retornar servicios de un empleado")
        void findByEmployeeId_ExistingEmployee_ReturnsServices() {
            // Given
            createAndPersistRelation(UUID.randomUUID(), true);
            createAndPersistRelation(UUID.randomUUID(), true);

            // When
            List<EmployeeServiceOffering> relations = employeeServiceOfferingRepository.findByEmployeeId(employee.getId());

            // Then
            assertThat(relations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndActiveTrue")
    class FindByEmployeeIdAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar solo relaciones activas")
        void findByEmployeeIdAndActiveTrue_ReturnsOnlyActive() {
            // Given
            createAndPersistRelation(UUID.randomUUID(), true);
            createAndPersistRelation(UUID.randomUUID(), false);

            // When
            List<EmployeeServiceOffering> activeRelations = employeeServiceOfferingRepository.findByEmployeeIdAndActiveTrue(employee.getId());

            // Then
            assertThat(activeRelations).hasSize(1);
            assertThat(activeRelations.get(0).getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByEmployeeIdAndServiceId")
    class FindByEmployeeIdAndServiceIdTests {

        @Test
        @DisplayName("Debe encontrar relación por empleado y servicio")
        void findByEmployeeIdAndServiceId_Existing_ReturnsRelation() {
            // Given
            EmployeeServiceOffering eso = createAndPersistRelation(serviceId, true);

            // When
            Optional<EmployeeServiceOffering> found = employeeServiceOfferingRepository.findByEmployeeIdAndServiceId(employee.getId(), serviceId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getServiceId()).isEqualTo(serviceId);
        }

        @Test
        @DisplayName("Debe retornar empty para combinación no existente")
        void findByEmployeeIdAndServiceId_NonExisting_ReturnsEmpty() {
            // When
            Optional<EmployeeServiceOffering> found = employeeServiceOfferingRepository.findByEmployeeIdAndServiceId(employee.getId(), UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmployeeIdAndServiceId")
    class ExistsByEmployeeIdAndServiceIdTests {

        @Test
        @DisplayName("Debe retornar true para relación existente")
        void existsByEmployeeIdAndServiceId_Existing_ReturnsTrue() {
            // Given
            createAndPersistRelation(serviceId, true);

            // When
            boolean exists = employeeServiceOfferingRepository.existsByEmployeeIdAndServiceId(employee.getId(), serviceId);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false para relación no existente")
        void existsByEmployeeIdAndServiceId_NonExisting_ReturnsFalse() {
            // When
            boolean exists = employeeServiceOfferingRepository.existsByEmployeeIdAndServiceId(employee.getId(), UUID.randomUUID());

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByServiceId")
    class FindByServiceIdTests {

        @Test
        @DisplayName("Debe retornar empleados que ofrecen un servicio")
        void findByServiceId_ExistingService_ReturnsEmployees() {
            // Given
            UUID service1 = UUID.randomUUID();
            createAndPersistRelation(service1, true);
            createAndPersistRelation(service1, true);

            // When
            List<EmployeeServiceOffering> relations = employeeServiceOfferingRepository.findByServiceId(service1);

            // Then
            assertThat(relations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar relación por ID")
        void deleteById_ExistingRelation_DeletesRelation() {
            // Given
            EmployeeServiceOffering eso = createAndPersistRelation(serviceId, true);
            UUID esoId = eso.getId();

            // When
            employeeServiceOfferingRepository.deleteById(esoId);
            entityManager.flush();

            // Then
            Optional<EmployeeServiceOffering> found = employeeServiceOfferingRepository.findById(esoId);
            assertThat(found).isEmpty();
        }
    }

    // Helper methods
    private EmployeeServiceOffering createAndPersistRelation(UUID servId, boolean active) {
        EmployeeServiceOffering eso = EmployeeServiceOffering.builder()
                .employee(employee)
                .serviceId(servId)
                .active(active)
                .build();
        return entityManager.persist(eso);
    }
}