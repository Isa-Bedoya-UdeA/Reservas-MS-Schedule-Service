package com.codefactory.reservasmsscheduleservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeServiceRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeServiceResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeWithServicesResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ServiceWithEmployeesResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.EmployeeServiceOfferingService;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controller for managing employee-service associations.
 * Provides endpoints for providers to manage which employees can perform which services.
 */
@RestController
@RequestMapping("/api/schedule/employee-services")
@RequiredArgsConstructor
@Tag(name = "Employee Service Offerings", description = "Endpoints for managing associations between employees and services")
public class EmployeeServiceOfferingController {

    private final EmployeeServiceOfferingService employeeServiceOfferingService;

    /**
     * Creates a new association between an employee and a service.
     * Only providers can create associations for their own employees and services.
     *
     * @param request the request containing employee and service IDs
     * @return the created association
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Create employee-service association",
            description = "Creates a new association between an employee and a service. " +
                    "The provider must own both the employee and the service. " +
                    "If an inactive association already exists, it will be reactivated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Association created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeServiceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not a provider or not owner of employee/service"),
            @ApiResponse(responseCode = "404", description = "Employee or service not found"),
            @ApiResponse(responseCode = "409", description = "Active association already exists")
    })
    public ResponseEntity<EntityModel<EmployeeServiceResponseDTO>> createAssociation(
            @Valid @RequestBody CreateEmployeeServiceRequestDTO request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        EmployeeServiceResponseDTO dto = employeeServiceOfferingService.createAssociation(request, providerId);
        EntityModel<EmployeeServiceResponseDTO> entityModel = EntityModel.of(dto,
            linkTo(methodOn(EmployeeServiceOfferingController.class).getServicesByEmployee(dto.getEmployeeId())).withRel("employee-services"),
            linkTo(methodOn(EmployeeServiceOfferingController.class).getEmployeesByService(dto.getServiceId())).withRel("service-employees"));
        return new ResponseEntity<>(entityModel, HttpStatus.CREATED);
    }

    /**
     * Deactivates an existing employee-service association.
     * Only the provider who owns the association can deactivate it.
     *
     * @param id the association ID
     * @return 204 No Content
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Deactivate employee-service association",
            description = "Deactivates an existing association (soft delete). " +
                    "Only the provider who owns the association can deactivate it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Association deactivated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not owner of the association"),
            @ApiResponse(responseCode = "404", description = "Association not found"),
            @ApiResponse(responseCode = "409", description = "Association is already inactive")
    })
    public ResponseEntity<Void> deactivateAssociation(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        employeeServiceOfferingService.deactivateAssociation(id, providerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates an existing employee-service association.
     * Only the provider who owns the association can activate it.
     *
     * @param id the association ID
     * @return 204 No Content
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Activate employee-service association",
            description = "Activates an existing inactive association. " +
                    "Only the provider who owns the association can activate it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Association activated successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not owner of the association"),
            @ApiResponse(responseCode = "404", description = "Association not found"),
            @ApiResponse(responseCode = "409", description = "Association is already active")
    })
    public ResponseEntity<Void> activateAssociation(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        employeeServiceOfferingService.activateAssociation(id, providerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently deletes an employee-service association.
     * Only the provider who owns the association can delete it.
     *
     * @param id the association ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Delete employee-service association (Hard Delete)",
            description = "Permanently deletes an association from the database. " +
                    "Only the provider who owns the association can delete it. " +
                    "This operation cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Association deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not owner of the association"),
            @ApiResponse(responseCode = "404", description = "Association not found")
    })
    public ResponseEntity<Void> deleteAssociation(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        employeeServiceOfferingService.deleteAssociation(id, providerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all employees associated with a service.
     * Only the provider who owns the service can view this.
     *
     * @param serviceId the service ID
     * @return the service with its associated employees
     */
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Get employees by service",
            description = "Retrieves all employees associated with a specific service. " +
                    "Only the provider who owns the service can access this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ServiceWithEmployeesResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not owner of the service"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<ServiceWithEmployeesResponseDTO> getEmployeesByService(@PathVariable UUID serviceId) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(employeeServiceOfferingService.getEmployeesByService(serviceId, providerId));
    }

    /**
     * Retrieves all services associated with an employee.
     * Only the provider who owns the employee can view this.
     *
     * @param employeeId the employee ID
     * @return the employee with their associated services
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
            summary = "Get services by employee",
            description = "Retrieves all services associated with a specific employee. " +
                    "Only the provider who owns the employee can access this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeWithServicesResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not owner of the employee"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeWithServicesResponseDTO> getServicesByEmployee(@PathVariable UUID employeeId) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(employeeServiceOfferingService.getServicesByEmployee(employeeId, providerId));
    }

    /**
     * Retrieves active employees associated with a service.
     * This is a public endpoint accessible to any authenticated user (e.g., clients).
     *
     * @param serviceId the service ID
     * @return the service with its active employees
     */
    @GetMapping("/service/{serviceId}/active")
    @Operation(
            summary = "Get active employees by service (Public)",
            description = "Retrieves only active employees associated with a specific service. " +
                    "This endpoint is public and accessible to any user (e.g., clients looking to make a reservation)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active employees retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ServiceWithEmployeesResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<ServiceWithEmployeesResponseDTO> getActiveEmployeesByService(@PathVariable UUID serviceId) {
        return ResponseEntity.ok(employeeServiceOfferingService.getActiveEmployeesByService(serviceId));
    }
}