package com.codefactory.reservasmsscheduleservice.controller;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeServiceRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeServiceResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeWithServicesResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ServiceWithEmployeesResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.EmployeeServiceOfferingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeServiceOfferingController using @ExtendWith(MockitoExtension).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - EmployeeServiceOfferingController (Unit)")
class EmployeeServiceOfferingControllerTest {

    @Mock
    private EmployeeServiceOfferingService employeeServiceOfferingService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployeeServiceOfferingController employeeServiceOfferingController;

    private UUID employeeId;
    private UUID serviceId;
    private UUID associationId;
    private UUID providerId;
    private EmployeeServiceResponseDTO employeeServiceResponse;
    private EmployeeWithServicesResponseDTO employeeWithServicesResponse;
    private ServiceWithEmployeesResponseDTO serviceWithEmployeesResponse;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        serviceId = UUID.randomUUID();
        associationId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        employeeServiceResponse = EmployeeServiceResponseDTO.builder()
                .id(associationId)
                .employeeId(employeeId)
                .serviceId(serviceId)
                .active(true)
                .assignmentDate(LocalDateTime.now())
                .build();

        employeeWithServicesResponse = EmployeeWithServicesResponseDTO.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .active(true)
                .services(List.of())
                .build();

        serviceWithEmployeesResponse = ServiceWithEmployeesResponseDTO.builder()
                .idServicio(serviceId)
                .idProveedor(providerId)
                .nombreServicio("Corte de cabello")
                .employees(List.of())
                .build();

        // Setup security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(providerId.toString());
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("createAssociation")
    class CreateAssociationTests {

        @Test
        @DisplayName("Debe crear asociación y retornar CREATED")
        void createAssociation_ReturnsCreated() {
            // Given
            CreateEmployeeServiceRequestDTO request = CreateEmployeeServiceRequestDTO.builder()
                    .employeeId(employeeId)
                    .serviceId(serviceId)
                    .build();

            when(employeeServiceOfferingService.createAssociation(any(), eq(providerId)))
                    .thenReturn(employeeServiceResponse);

            // When
            ResponseEntity<EntityModel<EmployeeServiceResponseDTO>> response = employeeServiceOfferingController.createAssociation(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().getEmployeeId()).isEqualTo(employeeId);
        }
    }

    @Nested
    @DisplayName("deactivateAssociation")
    class DeactivateAssociationTests {

        @Test
        @DisplayName("Debe desactivar asociación y retornar NO_CONTENT")
        void deactivateAssociation_ReturnsNoContent() {
            // Given
            doNothing().when(employeeServiceOfferingService).deactivateAssociation(associationId, providerId);

            // When
            ResponseEntity<Void> response = employeeServiceOfferingController.deactivateAssociation(associationId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(employeeServiceOfferingService).deactivateAssociation(associationId, providerId);
        }
    }

    @Nested
    @DisplayName("activateAssociation")
    class ActivateAssociationTests {

        @Test
        @DisplayName("Debe activar asociación y retornar NO_CONTENT")
        void activateAssociation_ReturnsNoContent() {
            // Given
            doNothing().when(employeeServiceOfferingService).activateAssociation(associationId, providerId);

            // When
            ResponseEntity<Void> response = employeeServiceOfferingController.activateAssociation(associationId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(employeeServiceOfferingService).activateAssociation(associationId, providerId);
        }
    }

    @Nested
    @DisplayName("deleteAssociation")
    class DeleteAssociationTests {

        @Test
        @DisplayName("Debe eliminar asociación y retornar NO_CONTENT")
        void deleteAssociation_ReturnsNoContent() {
            // Given
            doNothing().when(employeeServiceOfferingService).deleteAssociation(associationId, providerId);

            // When
            ResponseEntity<Void> response = employeeServiceOfferingController.deleteAssociation(associationId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(employeeServiceOfferingService).deleteAssociation(associationId, providerId);
        }
    }

    @Nested
    @DisplayName("getEmployeesByService")
    class GetEmployeesByServiceTests {

        @Test
        @DisplayName("Debe obtener empleados por servicio y retornar OK")
        void getEmployeesByService_ReturnsOk() {
            // Given
            when(employeeServiceOfferingService.getEmployeesByService(serviceId, providerId))
                    .thenReturn(serviceWithEmployeesResponse);

            // When
            ResponseEntity<ServiceWithEmployeesResponseDTO> response =
                    employeeServiceOfferingController.getEmployeesByService(serviceId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getIdServicio()).isEqualTo(serviceId);
        }
    }

    @Nested
    @DisplayName("getServicesByEmployee")
    class GetServicesByEmployeeTests {

        @Test
        @DisplayName("Debe obtener servicios por empleado y retornar OK")
        void getServicesByEmployee_ReturnsOk() {
            // Given
            when(employeeServiceOfferingService.getServicesByEmployee(employeeId, providerId))
                    .thenReturn(employeeWithServicesResponse);

            // When
            ResponseEntity<EmployeeWithServicesResponseDTO> response =
                    employeeServiceOfferingController.getServicesByEmployee(employeeId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(employeeId);
        }
    }

    @Nested
    @DisplayName("getActiveEmployeesByService")
    class GetActiveEmployeesByServiceTests {

        @Test
        @DisplayName("Debe obtener empleados activos por servicio y retornar OK")
        void getActiveEmployeesByService_ReturnsOk() {
            // Given
            when(employeeServiceOfferingService.getActiveEmployeesByService(serviceId))
                    .thenReturn(serviceWithEmployeesResponse);

            // When
            ResponseEntity<ServiceWithEmployeesResponseDTO> response =
                    employeeServiceOfferingController.getActiveEmployeesByService(serviceId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }
}