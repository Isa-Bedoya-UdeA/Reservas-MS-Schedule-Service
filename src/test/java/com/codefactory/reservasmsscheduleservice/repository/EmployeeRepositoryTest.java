package com.codefactory.reservasmsscheduleservice.repository;

import com.codefactory.reservasmsscheduleservice.entity.Employee;
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
 * Tests de integración para EmployeeRepository.
 * Utiliza @DataJpaTest con H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID providerId;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar empleado con todos los campos")
        void save_NewEmployee_SavesAllFields() {
            // Given
            Employee employee = Employee.builder()
                    .providerId(providerId)
                    .fullName("Juan Pérez")
                    .phone("3001234567")
                    .active(true)
                    .hireDate(LocalDateTime.now())
                    .notes("Empleado destacado")
                    .build();

            // When
            Employee saved = employeeRepository.save(employee);
            entityManager.flush();

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getFullName()).isEqualTo("Juan Pérez");
            assertThat(saved.getPhone()).isEqualTo("3001234567");
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Debe guardar empleado inactivo por defecto")
        void save_EmployeeInactive_DefaultsToInactive() {
            // Given
            Employee employee = Employee.builder()
                    .providerId(providerId)
                    .fullName("María López")
                    .active(false)
                    .build();

            // When
            Employee saved = employeeRepository.save(employee);
            entityManager.flush();

            // Then
            assertThat(saved.getActive()).isFalse();
        }

        @Test
        @DisplayName("Debe establecer fecha de creación automáticamente")
        void save_SetsCreatedAtAutomatically() {
            // Given
            Employee employee = Employee.builder()
                    .providerId(providerId)
                    .fullName("AutoDate Employee")
                    .build();

            // When
            Employee saved = employeeRepository.save(employee);
            entityManager.flush();

            // Then
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar empleado por ID")
        void findById_ExistingEmployee_ReturnsEmployee() {
            // Given
            Employee employee = createAndPersistEmployee("Test Employee", true);
            UUID employeeId = employee.getId();

            // When
            Optional<Employee> found = employeeRepository.findById(employeeId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getFullName()).isEqualTo("Test Employee");
        }

        @Test
        @DisplayName("Debe retornar empty para ID no existente")
        void findById_NonExistingId_ReturnsEmpty() {
            // When
            Optional<Employee> found = employeeRepository.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByProviderId")
    class FindByProviderIdTests {

        @Test
        @DisplayName("Debe retornar empleados de un proveedor específico")
        void findByProviderId_ExistingProvider_ReturnsEmployees() {
            // Given
            UUID otherProvider = UUID.randomUUID();
            createAndPersistEmployeeWithProvider("Employee 1", providerId, true);
            createAndPersistEmployeeWithProvider("Employee 2", providerId, true);
            createAndPersistEmployeeWithProvider("Other Provider", otherProvider, true);

            // When
            List<Employee> employees = employeeRepository.findByProviderId(providerId);

            // Then
            assertThat(employees).hasSize(2);
            assertThat(employees).allMatch(e -> e.getProviderId().equals(providerId));
        }

        @Test
        @DisplayName("Debe retornar lista vacía para proveedor sin empleados")
        void findByProviderId_NoEmployees_ReturnsEmptyList() {
            // When
            List<Employee> employees = employeeRepository.findByProviderId(UUID.randomUUID());

            // Then
            assertThat(employees).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByProviderIdAndActiveTrue")
    class FindByProviderIdAndActiveTrueTests {

        @Test
        @DisplayName("Debe retornar solo empleados activos de un proveedor")
        void findByProviderIdAndActiveTrue_ReturnsOnlyActive() {
            // Given
            createAndPersistEmployeeWithProvider("Active Employee", providerId, true);
            createAndPersistEmployeeWithProvider("Inactive Employee", providerId, false);

            // When
            List<Employee> activeEmployees = employeeRepository.findByProviderIdAndActiveTrue(providerId);

            // Then
            assertThat(activeEmployees).hasSize(1);
            assertThat(activeEmployees.get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar lista vacía si todos los empleados están inactivos")
        void findByProviderIdAndActiveTrue_AllInactive_ReturnsEmptyList() {
            // Given
            createAndPersistEmployeeWithProvider("Inactive 1", providerId, false);
            createAndPersistEmployeeWithProvider("Inactive 2", providerId, false);

            // When
            List<Employee> activeEmployees = employeeRepository.findByProviderIdAndActiveTrue(providerId);

            // Then
            assertThat(activeEmployees).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndProviderId")
    class FindByIdAndProviderIdTests {

        @Test
        @DisplayName("Debe retornar empleado si ID y provider coinciden")
        void findByIdAndProviderId_Existing_ReturnsEmployee() {
            // Given
            Employee employee = createAndPersistEmployeeWithProvider("Test Employee", providerId, true);

            // When
            Optional<Employee> found = employeeRepository.findByIdAndProviderId(employee.getId(), providerId);

            // Then
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Debe retornar empty si provider no coincide")
        void findByIdAndProviderId_WrongProvider_ReturnsEmpty() {
            // Given
            Employee employee = createAndPersistEmployeeWithProvider("Test Employee", providerId, true);
            UUID otherProvider = UUID.randomUUID();

            // When
            Optional<Employee> found = employeeRepository.findByIdAndProviderId(employee.getId(), otherProvider);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByIdAndProviderId")
    class ExistsByIdAndProviderIdTests {

        @Test
        @DisplayName("Debe retornar true si existe combinación")
        void existsByIdAndProviderId_Existing_ReturnsTrue() {
            // Given
            Employee employee = createAndPersistEmployeeWithProvider("Test Employee", providerId, true);

            // When
            boolean exists = employeeRepository.existsByIdAndProviderId(employee.getId(), providerId);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si no existe")
        void existsByIdAndProviderId_NonExisting_ReturnsFalse() {
            // When
            boolean exists = employeeRepository.existsByIdAndProviderId(UUID.randomUUID(), providerId);

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar empleado por ID")
        void deleteById_ExistingEmployee_DeletesEmployee() {
            // Given
            Employee employee = createAndPersistEmployee("To Delete", true);
            UUID employeeId = employee.getId();

            // When
            employeeRepository.deleteById(employeeId);
            entityManager.flush();

            // Then
            Optional<Employee> found = employeeRepository.findById(employeeId);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar todos los empleados")
        void findAll_ReturnsAllEmployees() {
            // Given
            createAndPersistEmployee("Employee 1", true);
            createAndPersistEmployee("Employee 2", false);
            createAndPersistEmployee("Employee 3", true);

            // When
            List<Employee> allEmployees = employeeRepository.findAll();

            // Then
            assertThat(allEmployees).hasSize(3);
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay empleados")
        void findAll_Empty_ReturnsEmptyList() {
            // When
            List<Employee> allEmployees = employeeRepository.findAll();

            // Then
            assertThat(allEmployees).isEmpty();
        }
    }

    @Nested
    @DisplayName("count")
    class CountTests {

        @Test
        @DisplayName("Debe retornar count correcto")
        void count_ReturnsCorrectCount() {
            // Given
            long initialCount = employeeRepository.count();
            createAndPersistEmployee("Count 1", true);
            createAndPersistEmployee("Count 2", false);
            entityManager.flush();

            // When
            long newCount = employeeRepository.count();

            // Then
            assertThat(newCount).isEqualTo(initialCount + 2);
        }
    }

    @Nested
    @DisplayName("findByActiveTrue")
    class FindByActiveTrueTests {

        @Test
        @DisplayName("Debe retornar solo empleados activos de todos los proveedores")
        void findByActiveTrue_ReturnsOnlyActive() {
            // Given
            UUID provider1 = UUID.randomUUID();
            UUID provider2 = UUID.randomUUID();
            createAndPersistEmployeeWithProvider("Active 1", provider1, true);
            createAndPersistEmployeeWithProvider("Inactive 1", provider1, false);
            createAndPersistEmployeeWithProvider("Active 2", provider2, true);
            createAndPersistEmployeeWithProvider("Inactive 2", provider2, false);

            // When
            List<Employee> activeEmployees = employeeRepository.findByActiveTrue();

            // Then
            assertThat(activeEmployees).hasSize(2);
            assertThat(activeEmployees).allMatch(Employee::getActive);
        }
    }

    // Helper methods
    private Employee createAndPersistEmployee(String name, boolean active) {
        return createAndPersistEmployeeWithProvider(name, providerId, active);
    }

    private Employee createAndPersistEmployeeWithProvider(String name, UUID provId, boolean active) {
        Employee employee = Employee.builder()
                .providerId(provId)
                .fullName(name)
                .phone("3001234567")
                .active(active)
                .hireDate(LocalDateTime.now())
                .notes("Test employee")
                .build();
        return entityManager.persist(employee);
    }
}