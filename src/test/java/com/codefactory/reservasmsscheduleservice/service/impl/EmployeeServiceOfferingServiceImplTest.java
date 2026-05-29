package com.codefactory.reservasmsscheduleservice.service.impl;

import com.codefactory.reservasmsscheduleservice.client.CatalogClientWrapper;
import com.codefactory.reservasmsscheduleservice.dto.external.ExternalServiceOfferingDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeServiceRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeServiceResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeWithServicesResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ServiceWithEmployeesResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.EmployeeServiceOffering;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeServiceAlreadyActiveException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeServiceAlreadyExistsException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeServiceAlreadyInactiveException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeServiceNotFoundException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeServiceOwnershipException;
import com.codefactory.reservasmsscheduleservice.mapper.EmployeeMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeServiceOfferingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceOfferingServiceImpl using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - EmployeeServiceOfferingServiceImpl (Unit)")
class EmployeeServiceOfferingServiceImplTest {

    @Mock
    private EmployeeServiceOfferingRepository employeeServiceOfferingRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CatalogClientWrapper catalogClientWrapper;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceOfferingServiceImpl employeeServiceOfferingService;

    private UUID employeeId;
    private UUID serviceId;
    private UUID associationId;
    private UUID providerId;
    private Employee employee;
    private EmployeeServiceOffering association;
    private ExternalServiceOfferingDTO serviceDTO;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        associationId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        employee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        association = EmployeeServiceOffering.builder()
                .id(associationId)
                .employee(employee)
                .serviceId(serviceId)
                .active(true)
                .assignmentDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        serviceDTO = ExternalServiceOfferingDTO.builder()
                .idServicio(serviceId)
                .idProveedor(providerId)
                .nombreServicio("Corte de cabello")
                .duracionMinutos(30)
                .precio(BigDecimal.valueOf(25000.0))
                .descripcion("Corte básico")
                .activo(true)
                .capacidadMaxima(1)
                .build();
    }

    @Nested
    @DisplayName("createAssociation")
    class CreateAssociationTests {

        @Test
        @DisplayName("Debe crear asociación exitosamente")
        void createAssociation_ValidRequest_ReturnsAssociation() {
            // Given
            CreateEmployeeServiceRequestDTO request = CreateEmployeeServiceRequestDTO.builder()
                    .employeeId(employeeId)
                    .serviceId(serviceId)
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(catalogClientWrapper.validateServiceOwnership(serviceId, providerId)).thenReturn(serviceDTO);
            when(employeeServiceOfferingRepository.findByEmployeeIdAndServiceId(employeeId, serviceId))
                    .thenReturn(Optional.empty());
            when(employeeServiceOfferingRepository.save(any(EmployeeServiceOffering.class))).thenReturn(association);

            // When
            EmployeeServiceResponseDTO result = employeeServiceOfferingService.createAssociation(request, providerId);

            // Then
            assertThat(result).isNotNull();
            verify(employeeServiceOfferingRepository).save(any(EmployeeServiceOffering.class));
        }

        @Test
        @DisplayName("Debe reactivar asociación si ya existe pero está inactiva")
        void createAssociation_InactiveExists_Reactivates() {
            // Given
            association.setActive(false);
            CreateEmployeeServiceRequestDTO request = CreateEmployeeServiceRequestDTO.builder()
                    .employeeId(employeeId)
                    .serviceId(serviceId)
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(catalogClientWrapper.validateServiceOwnership(serviceId, providerId)).thenReturn(serviceDTO);
            when(employeeServiceOfferingRepository.findByEmployeeIdAndServiceId(employeeId, serviceId))
                    .thenReturn(Optional.of(association));
            when(employeeServiceOfferingRepository.save(any(EmployeeServiceOffering.class))).thenReturn(association);

            // When
            EmployeeServiceResponseDTO result = employeeServiceOfferingService.createAssociation(request, providerId);

            // Then
            assertThat(association.getActive()).isTrue();
            verify(employeeServiceOfferingRepository).save(association);
        }

        @Test
        @DisplayName("Debe lanzar excepción si asociación activa ya existe")
        void createAssociation_ActiveExists_ThrowsException() {
            // Given
            CreateEmployeeServiceRequestDTO request = CreateEmployeeServiceRequestDTO.builder()
                    .employeeId(employeeId)
                    .serviceId(serviceId)
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(catalogClientWrapper.validateServiceOwnership(serviceId, providerId)).thenReturn(serviceDTO);
            when(employeeServiceOfferingRepository.findByEmployeeIdAndServiceId(employeeId, serviceId))
                    .thenReturn(Optional.of(association));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.createAssociation(request, providerId))
                    .isInstanceOf(EmployeeServiceAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void createAssociation_EmployeeNotFound_ThrowsException() {
            // Given
            CreateEmployeeServiceRequestDTO request = CreateEmployeeServiceRequestDTO.builder()
                    .employeeId(employeeId)
                    .serviceId(serviceId)
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.createAssociation(request, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no pertenece al proveedor")
        void createAssociation_NotOwner_ThrowsException() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            CreateEmployeeServiceRequestDTO request = CreateEmployeeServiceRequestDTO.builder()
                    .employeeId(employeeId)
                    .serviceId(serviceId)
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.createAssociation(request, otherProviderId))
                    .isInstanceOf(EmployeeServiceOwnershipException.class);
        }
    }

    @Nested
    @DisplayName("deactivateAssociation")
    class DeactivateAssociationTests {

        @Test
        @DisplayName("Debe desactivar asociación exitosamente")
        void deactivateAssociation_ValidAssociation_Deactivates() {
            // Given
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));
            when(employeeServiceOfferingRepository.save(any(EmployeeServiceOffering.class))).thenReturn(association);

            // When
            employeeServiceOfferingService.deactivateAssociation(associationId, providerId);

            // Then
            assertThat(association.getActive()).isFalse();
            verify(employeeServiceOfferingRepository).save(association);
        }

        @Test
        @DisplayName("Debe lanzar excepción si asociación no existe")
        void deactivateAssociation_NotFound_ThrowsException() {
            // Given
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.deactivateAssociation(associationId, providerId))
                    .isInstanceOf(EmployeeServiceNotFoundException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si ya está inactiva")
        void deactivateAssociation_AlreadyInactive_ThrowsException() {
            // Given
            association.setActive(false);
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.deactivateAssociation(associationId, providerId))
                    .isInstanceOf(EmployeeServiceAlreadyInactiveException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void deactivateAssociation_NotOwner_ThrowsException() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.deactivateAssociation(associationId, otherProviderId))
                    .isInstanceOf(EmployeeServiceOwnershipException.class);
        }
    }

    @Nested
    @DisplayName("activateAssociation")
    class ActivateAssociationTests {

        @Test
        @DisplayName("Debe activar asociación exitosamente")
        void activateAssociation_InactiveAssociation_Activates() {
            // Given
            association.setActive(false);
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));
            when(employeeServiceOfferingRepository.save(any(EmployeeServiceOffering.class))).thenReturn(association);

            // When
            employeeServiceOfferingService.activateAssociation(associationId, providerId);

            // Then
            assertThat(association.getActive()).isTrue();
            verify(employeeServiceOfferingRepository).save(association);
        }

        @Test
        @DisplayName("Debe lanzar excepción si ya está activa")
        void activateAssociation_AlreadyActive_ThrowsException() {
            // Given
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.activateAssociation(associationId, providerId))
                    .isInstanceOf(EmployeeServiceAlreadyActiveException.class);
        }
    }

    @Nested
    @DisplayName("deleteAssociation")
    class DeleteAssociationTests {

        @Test
        @DisplayName("Debe eliminar asociación exitosamente")
        void deleteAssociation_ValidAssociation_Deletes() {
            // Given
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));
            doNothing().when(employeeServiceOfferingRepository).delete(association);

            // When
            employeeServiceOfferingService.deleteAssociation(associationId, providerId);

            // Then
            verify(employeeServiceOfferingRepository).delete(association);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void deleteAssociation_NotOwner_ThrowsException() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeServiceOfferingRepository.findById(associationId)).thenReturn(Optional.of(association));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.deleteAssociation(associationId, otherProviderId))
                    .isInstanceOf(EmployeeServiceOwnershipException.class);
        }
    }

    @Nested
    @DisplayName("getEmployeesByService")
    class GetEmployeesByServiceTests {

        @Test
        @DisplayName("Debe obtener empleados por servicio")
        void getEmployeesByService_ValidService_ReturnsServiceWithEmployees() {
            // Given
            EmployeeResponseDTO employeeResponse = EmployeeResponseDTO.builder()
                    .id(employeeId)
                    .providerId(providerId)
                    .fullName("Juan Pérez")
                    .active(true)
                    .build();

            when(catalogClientWrapper.validateServiceOwnership(serviceId, providerId)).thenReturn(serviceDTO);
            when(employeeServiceOfferingRepository.findByServiceId(serviceId)).thenReturn(List.of(association));
            when(employeeMapper.toDto(employee)).thenReturn(employeeResponse);

            // When
            ServiceWithEmployeesResponseDTO result = employeeServiceOfferingService.getEmployeesByService(serviceId, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmployees()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getServicesByEmployee")
    class GetServicesByEmployeeTests {

        @Test
        @DisplayName("Debe obtener servicios por empleado")
        void getServicesByEmployee_ValidEmployee_ReturnsEmployeeWithServices() {
            // Given
            EmployeeResponseDTO employeeResponse = EmployeeResponseDTO.builder()
                    .id(employeeId)
                    .providerId(providerId)
                    .fullName("Juan Pérez")
                    .active(true)
                    .build();

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeServiceOfferingRepository.findByEmployeeIdAndActiveTrue(employeeId))
                    .thenReturn(List.of(association));
            when(catalogClientWrapper.getServiceOrThrow(serviceId)).thenReturn(serviceDTO);

            // When
            EmployeeWithServicesResponseDTO result = employeeServiceOfferingService.getServicesByEmployee(employeeId, providerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getServices()).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no pertenece al proveedor")
        void getServicesByEmployee_NotOwner_ThrowsException() {
            // Given
            UUID otherProviderId = UUID.randomUUID();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // When/Then
            assertThatThrownBy(() -> employeeServiceOfferingService.getServicesByEmployee(employeeId, otherProviderId))
                    .isInstanceOf(EmployeeServiceOwnershipException.class);
        }
    }

    @Nested
    @DisplayName("getActiveEmployeesByService")
    class GetActiveEmployeesByServiceTests {

        @Test
        @DisplayName("Debe obtener empleados activos por servicio")
        void getActiveEmployeesByService_ReturnsActiveEmployees() {
            // Given
            EmployeeResponseDTO employeeResponse = EmployeeResponseDTO.builder()
                    .id(employeeId)
                    .providerId(providerId)
                    .fullName("Juan Pérez")
                    .active(true)
                    .build();

            when(catalogClientWrapper.getServiceOrThrow(serviceId)).thenReturn(serviceDTO);
            when(employeeServiceOfferingRepository.findByServiceIdAndActiveTrue(serviceId))
                    .thenReturn(List.of(association));
            when(employeeMapper.toDto(employee)).thenReturn(employeeResponse);

            // When
            ServiceWithEmployeesResponseDTO result = employeeServiceOfferingService.getActiveEmployeesByService(serviceId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmployees()).hasSize(1);
        }
    }
}