package com.codefactory.reservasmsscheduleservice.controller;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.EmployeeService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeController using @ExtendWith(MockitoExtension).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - EmployeeController (Unit)")
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployeeController employeeController;

    private UUID employeeId;
    private UUID providerId;
    private EmployeeResponseDTO employeeResponse;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        employeeResponse = EmployeeResponseDTO.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Juan Pérez")
                .phone("3001234567")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        // Setup security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(providerId.toString());
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployeeTests {

        @Test
        @DisplayName("createEmployee returns CREATED status")
        void createEmployee_ReturnsCreated() {
            CreateEmployeeRequestDTO request = CreateEmployeeRequestDTO.builder()
                    .fullName("Nuevo Empleado")
                    .phone("3009876543")
                    .build();

            when(employeeService.createEmployee(any(CreateEmployeeRequestDTO.class), eq(providerId))).thenReturn(employeeResponse);

            ResponseEntity<EntityModel<EmployeeResponseDTO>> response = employeeController.createEmployee(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("updateEmployee returns OK status")
        void updateEmployee_ReturnsOk() {
            UpdateEmployeeRequestDTO request = UpdateEmployeeRequestDTO.builder()
                    .fullName("Empleado Actualizado")
                    .phone("3001112222")
                    .build();

            EmployeeResponseDTO updated = EmployeeResponseDTO.builder()
                    .id(employeeId)
                    .providerId(providerId)
                    .fullName("Empleado Actualizado")
                    .phone("3001112222")
                    .active(true)
                    .build();

            when(employeeService.updateEmployee(eq(employeeId), any(UpdateEmployeeRequestDTO.class), eq(providerId))).thenReturn(updated);

            ResponseEntity<EntityModel<EmployeeResponseDTO>> response = employeeController.updateEmployee(employeeId, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().getFullName()).isEqualTo("Empleado Actualizado");
        }
    }

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("deleteEmployee returns NO_CONTENT")
        void deleteEmployee_ReturnsNoContent() {
            doNothing().when(employeeService).deleteEmployee(employeeId, providerId);

            ResponseEntity<Void> response = employeeController.deleteEmployee(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(employeeService).deleteEmployee(employeeId, providerId);
        }
    }

    @Nested
    @DisplayName("deactivateEmployee")
    class DeactivateEmployeeTests {

        @Test
        @DisplayName("deactivateEmployee returns NO_CONTENT")
        void deactivateEmployee_ReturnsNoContent() {
            doNothing().when(employeeService).deactivateEmployee(employeeId, providerId);

            ResponseEntity<Void> response = employeeController.deactivateEmployee(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(employeeService).deactivateEmployee(employeeId, providerId);
        }
    }

    @Nested
    @DisplayName("activateEmployee")
    class ActivateEmployeeTests {

        @Test
        @DisplayName("activateEmployee returns NO_CONTENT")
        void activateEmployee_ReturnsNoContent() {
            doNothing().when(employeeService).activateEmployee(employeeId, providerId);

            ResponseEntity<Void> response = employeeController.activateEmployee(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(employeeService).activateEmployee(employeeId, providerId);
        }
    }

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeByIdTests {

        @Test
        @DisplayName("getEmployeeById returns employee when found")
        void getEmployeeById_ReturnsEmployee() {
            when(employeeService.getEmployeeById(employeeId, providerId)).thenReturn(employeeResponse);

            ResponseEntity<EntityModel<EmployeeResponseDTO>> response = employeeController.getEmployeeById(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().getId()).isEqualTo(employeeId);
        }
    }

    @Nested
    @DisplayName("getEmployeesByProvider")
    class GetEmployeesByProviderTests {

        @Test
        @DisplayName("getEmployeesByProvider returns list of employees")
        void getEmployeesByProvider_ReturnsList() {
            when(employeeService.getEmployeesByProvider(providerId, providerId)).thenReturn(List.of(employeeResponse));

            ResponseEntity<CollectionModel<EntityModel<EmployeeResponseDTO>>> response = employeeController.getEmployeesByProvider();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getActiveEmployeesByProvider")
    class GetActiveEmployeesByProviderTests {

        @Test
        @DisplayName("getActiveEmployeesByProvider returns list of active employees")
        void getActiveEmployeesByProvider_ReturnsList() {
            when(employeeService.getActiveEmployees()).thenReturn(List.of(employeeResponse));

            ResponseEntity<CollectionModel<EntityModel<EmployeeResponseDTO>>> response = employeeController.getActiveEmployeesByProvider();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("isEmployeeActive")
    class IsEmployeeActiveTests {

        @Test
        @DisplayName("isEmployeeActive returns true when active")
        void isEmployeeActive_ReturnsTrue() {
            when(employeeService.isEmployeeActive(employeeId)).thenReturn(true);

            ResponseEntity<Boolean> response = employeeController.isEmployeeActive(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
        }

        @Test
        @DisplayName("isEmployeeActive returns false when inactive")
        void isEmployeeActive_ReturnsFalse() {
            when(employeeService.isEmployeeActive(employeeId)).thenReturn(false);

            ResponseEntity<Boolean> response = employeeController.isEmployeeActive(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
        }
    }

    @Nested
    @DisplayName("getEmployeeProviderId")
    class GetEmployeeProviderIdTests {

        @Test
        @DisplayName("getEmployeeProviderId returns provider ID")
        void getEmployeeProviderId_ReturnsProviderId() {
            when(employeeService.getEmployeeProviderId(employeeId)).thenReturn(providerId);

            ResponseEntity<UUID> response = employeeController.getEmployeeProviderId(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(providerId);
        }
    }

    @Nested
    @DisplayName("getEmployeeBasicInfo")
    class GetEmployeeBasicInfoTests {

        @Test
        @DisplayName("getEmployeeBasicInfo returns employee info")
        void getEmployeeBasicInfo_ReturnsInfo() {
            when(employeeService.getEmployeeByIdPublic(employeeId)).thenReturn(employeeResponse);

            ResponseEntity<EmployeeResponseDTO> response = employeeController.getEmployeeBasicInfo(employeeId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }
    }
}