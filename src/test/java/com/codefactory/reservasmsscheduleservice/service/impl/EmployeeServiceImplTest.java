package com.codefactory.reservasmsscheduleservice.service.impl;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.exception.BusinessException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.mapper.EmployeeMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - EmployeeServiceImpl (Unit)")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private UUID employeeId;
    private UUID providerId;
    private Employee employee;
    private EmployeeResponseDTO employeeResponseDTO;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        employee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        employeeResponseDTO = EmployeeResponseDTO.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Debe crear empleado exitosamente")
        void createEmployee_ValidRequest_ReturnsEmployee() {
            // Given
            CreateEmployeeRequestDTO request = CreateEmployeeRequestDTO.builder()
                    .fullName("Juan Pérez")
                    .phone("3001234567")
                    .build();

            when(employeeMapper.toEntity(request)).thenReturn(employee);
            when(employeeMapper.toDto(any(Employee.class))).thenReturn(employeeResponseDTO);
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            EmployeeResponseDTO result = employeeService.createEmployee(request, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("Juan Pérez");
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("Debe establecer providerId y active al crear")
        void createEmployee_SetsProviderIdAndActive() {
            // Given
            CreateEmployeeRequestDTO request = CreateEmployeeRequestDTO.builder()
                    .fullName("María López")
                    .phone("3009876543")
                    .build();

            when(employeeMapper.toEntity(request)).thenReturn(employee);
            when(employeeMapper.toDto(any(Employee.class))).thenReturn(employeeResponseDTO);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee saved = invocation.getArgument(0);
                assertThat(saved.getProviderId()).isEqualTo(providerId);
                assertThat(saved.getActive()).isTrue();
                return saved;
            });

            // When
            employeeService.createEmployee(request, providerId);

            // Then
            verify(employeeRepository).save(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Debe actualizar empleado exitosamente")
        void updateEmployee_ValidRequest_ReturnsUpdatedEmployee() {
            // Given
            UpdateEmployeeRequestDTO request = UpdateEmployeeRequestDTO.builder()
                    .fullName("Juan Actualizado")
                    .phone("3001112222")
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeMapper).updateEntityFromDto(request, employee);
            when(employeeMapper.toDto(any(Employee.class))).thenReturn(employeeResponseDTO);
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            EmployeeResponseDTO result = employeeService.updateEmployee(employeeId, request, providerId);

            // Then
            assertThat(result).isNotNull();
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void updateEmployee_EmployeeNotFound_ThrowsException() {
            // Given
            UpdateEmployeeRequestDTO request = UpdateEmployeeRequestDTO.builder()
                    .fullName("Test")
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, request, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void updateEmployee_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            UpdateEmployeeRequestDTO request = UpdateEmployeeRequestDTO.builder()
                    .fullName("Test")
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeService.updateEmployee(employeeId, request, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deactivateEmployee")
    class DeactivateEmployeeTests {

        @Test
        @DisplayName("Debe desactivar empleado exitosamente")
        void deactivateEmployee_ValidEmployee_Deactivates() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            employeeService.deactivateEmployee(employeeId, providerId);

            // Then
            assertThat(employee.getActive()).isFalse();
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Debe lanzar excepción si ya está inactivo")
        void deactivateEmployee_AlreadyInactive_ThrowsException() {
            // Given
            employee.setActive(false);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeService.deactivateEmployee(employeeId, providerId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void deactivateEmployee_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeService.deactivateEmployee(employeeId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("activateEmployee")
    class ActivateEmployeeTests {

        @Test
        @DisplayName("Debe activar empleado exitosamente")
        void activateEmployee_InactiveEmployee_Activates() {
            // Given
            employee.setActive(false);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

            // When
            employeeService.activateEmployee(employeeId, providerId);

            // Then
            assertThat(employee.getActive()).isTrue();
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Debe lanzar excepción si ya está activo")
        void activateEmployee_AlreadyActive_ThrowsException() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeService.activateEmployee(employeeId, providerId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("Debe obtener empleado por ID")
        void getEmployeeById_ExistingEmployee_ReturnsEmployee() {
            // Given
            when(employeeRepository.findByIdAndProviderId(employeeId, providerId)).thenReturn(Optional.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(employeeResponseDTO);

            // When
            EmployeeResponseDTO result = employeeService.getEmployeeById(employeeId, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(employeeId);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no existe")
        void getEmployeeById_NotFound_ThrowsException() {
            // Given
            when(employeeRepository.findByIdAndProviderId(employeeId, providerId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> employeeService.getEmployeeById(employeeId, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getEmployeesByProvider")
    class GetEmployeesByProviderTests {

        @Test
        @DisplayName("Debe obtener empleados por proveedor")
        void getEmployeesByProvider_ValidProvider_ReturnsEmployees() {
            // Given
            when(employeeRepository.findByProviderId(providerId)).thenReturn(List.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(employeeResponseDTO);

            // When
            List<EmployeeResponseDTO> result = employeeService.getEmployeesByProvider(providerId, providerId);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si proveedor diferente")
        void getEmployeesByProvider_DifferentProvider_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();

            // When/Then
            assertThatThrownBy(() -> employeeService.getEmployeesByProvider(providerId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("isEmployeeActive")
    class IsEmployeeActiveTests {

        @Test
        @DisplayName("Debe retornar true si empleado está activo")
        void isEmployeeActive_ActiveEmployee_ReturnsTrue() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When
            boolean result = employeeService.isEmployeeActive(employeeId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si empleado no existe")
        void isEmployeeActive_NonExistingEmployee_ReturnsFalse() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When
            boolean result = employeeService.isEmployeeActive(employeeId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getActiveEmployees")
    class GetActiveEmployeesTests {

        @Test
        @DisplayName("Debe retornar solo empleados activos")
        void getActiveEmployees_ReturnsOnlyActive() {
            // Given
            when(employeeRepository.findByActiveTrue()).thenReturn(List.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(employeeResponseDTO);

            // When
            List<EmployeeResponseDTO> result = employeeService.getActiveEmployees();

            // Then
            assertThat(result).hasSize(1);
            verify(employeeRepository).findByActiveTrue();
        }
    }

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Debe eliminar empleado exitosamente")
        void deleteEmployee_ValidEmployee_Deletes() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // When
            employeeService.deleteEmployee(employeeId, providerId);

            // Then
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no existe")
        void deleteEmployee_NotFound_ThrowsException() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void deleteEmployee_NotOwner_ThrowsAccessDenied() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeService.deleteEmployee(employeeId, otherProviderId))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("getEmployeeProviderId")
    class GetEmployeeProviderIdTests {

        @Test
        @DisplayName("Debe retornar provider ID del empleado")
        void getEmployeeProviderId_ExistingEmployee_ReturnsProviderId() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When
            UUID result = employeeService.getEmployeeProviderId(employeeId);

            // Then
            assertThat(result).isEqualTo(providerId);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void getEmployeeProviderId_NotFound_ThrowsException() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> employeeService.getEmployeeProviderId(employeeId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getEmployeeByIdPublic")
    class GetEmployeeByIdPublicTests {

        @Test
        @DisplayName("Debe obtener empleado sin verificación de propiedad")
        void getEmployeeByIdPublic_ExistingEmployee_ReturnsEmployee() {
            // Given
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(employeeResponseDTO);

            // When
            EmployeeResponseDTO result = employeeService.getEmployeeByIdPublic(employeeId);

            // Then
            assertThat(result).isNotNull();
        }
    }
}