package com.codefactory.reservasmsscheduleservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.EmployeeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedule/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Endpoints for employee management")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(
        summary = "Create employee",
        description = "Creates a new employee for the authenticated provider. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Employee created successfully", content = @Content(schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role")
    })
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody CreateEmployeeRequestDTO request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        return new ResponseEntity<>(employeeService.createEmployee(request, providerId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update employee",
        description = "Updates an existing employee. Only the provider who created it can modify it. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee updated successfully", content = @Content(schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not the creator of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRequestDTO request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(employeeService.updateEmployee(id, request, providerId));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete employee (Hard Delete)",
        description = "Permanently deletes an employee from the database. Only the provider who created it can delete it. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not the creator of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        employeeService.deleteEmployee(id, providerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
        summary = "Deactivate employee (Soft Delete)",
        description = "Deactivates an employee (changes status to INACTIVE). Only the provider who created it can deactivate it. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee deactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not the creator of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Employee is already inactive")
    })
    public ResponseEntity<Void> deactivateEmployee(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        employeeService.deactivateEmployee(id, providerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Activate employee",
        description = "Activates an employee (changes status to ACTIVE). Only the provider who created it can activate it. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Employee activated successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not the creator of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Employee is already active")
    })
    public ResponseEntity<Void> activateEmployee(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        employeeService.activateEmployee(id, providerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get employee by ID",
        description = "Returns a specific employee by ID. Only the provider who owns it can view it. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee returned successfully", content = @Content(schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(employeeService.getEmployeeById(id, providerId));
    }

    @GetMapping
    @Operation(
        summary = "List provider's employees",
        description = "Returns all employees (active and inactive) of the authenticated provider. Requires PROVEEDOR role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of employees returned successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role")
    })
    public ResponseEntity<List<EmployeeResponseDTO>> getEmployeesByProvider() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        return ResponseEntity.ok(employeeService.getEmployeesByProvider(providerId, providerId));
    }

    @GetMapping("/active")
    @Operation(
        summary = "List active employees",
        description = "Returns only active employees of all providers. Public endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of active employees returned successfully")
    })
    public ResponseEntity<List<EmployeeResponseDTO>> getActiveEmployeesByProvider() {
        return ResponseEntity.ok(employeeService.getActiveEmployees());
    }

    // ==================== ENDPOINTS FOR RESERVATION SERVICE ====================

    /**
     * Check if an employee is active. Public endpoint for other microservices.
     */
    @GetMapping("/{id}/active")
    @Operation(
        summary = "Check if employee is active",
        description = "Returns whether an employee is active. Public endpoint for other microservices."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee active status returned")
    })
    public ResponseEntity<Boolean> isEmployeeActive(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.isEmployeeActive(id));
    }

    /**
     * Get the provider ID for an employee. Public endpoint for other microservices.
     */
    @GetMapping("/{id}/provider")
    @Operation(
        summary = "Get employee provider ID",
        description = "Returns the provider ID associated with an employee. Public endpoint for other microservices."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Provider ID returned"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<UUID> getEmployeeProviderId(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getEmployeeProviderId(id));
    }

    /**
     * Get basic employee info (name). Public endpoint for other microservices.
     * Only returns id, name and providerId - no sensitive data.
     */
    @GetMapping("/{id}/info")
    @Operation(
        summary = "Get employee basic info",
        description = "Returns basic employee info (id, name). Public endpoint for other microservices."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee info returned"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeResponseDTO> getEmployeeBasicInfo(@PathVariable UUID id) {
        // Only return basic info without provider ownership check
        EmployeeResponseDTO employee = employeeService.getEmployeeByIdPublic(id);
        return ResponseEntity.ok(employee);
    }
}
