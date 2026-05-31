package com.codefactory.reservasmsscheduleservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.WorkScheduleResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.WorkScheduleService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/schedule/work-schedules")
@RequiredArgsConstructor
@Tag(name = "Work Schedule Management", description = "API para gestionar horarios laborales recurrentes de empleados")
@SecurityRequirement(name = "bearerAuth")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @PostMapping
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Create work schedule",
        description = "Crea un nuevo horario laboral recurrente para un empleado. Solo el proveedor dueño del empleado puede crear horarios."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Work schedule created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Work schedule conflicts with existing schedule")
    })
    public ResponseEntity<EntityModel<WorkScheduleResponseDTO>> createWorkSchedule(
            @Valid @RequestBody CreateWorkScheduleRequestDTO request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        WorkScheduleResponseDTO dto = workScheduleService.createWorkSchedule(request, providerId);
        EntityModel<WorkScheduleResponseDTO> entityModel = EntityModel.of(dto,
            linkTo(methodOn(WorkScheduleController.class).getWorkScheduleById(dto.getId())).withSelfRel(),
            linkTo(methodOn(WorkScheduleController.class).getWorkSchedulesByEmployee(dto.getEmployeeId())).withRel("employee-schedules"));
        return ResponseEntity.status(201).body(entityModel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Update work schedule",
        description = "Actualiza un horario laboral existente. Solo el proveedor dueño del empleado puede modificarlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Work schedule updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Work schedule not found")
    })
    public ResponseEntity<EntityModel<WorkScheduleResponseDTO>> updateWorkSchedule(
            @Parameter(description = "ID del horario laboral") @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkScheduleRequestDTO request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        WorkScheduleResponseDTO dto = workScheduleService.updateWorkSchedule(id, request, providerId);
        EntityModel<WorkScheduleResponseDTO> entityModel = EntityModel.of(dto,
            linkTo(methodOn(WorkScheduleController.class).getWorkScheduleById(id)).withSelfRel());
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Delete work schedule (Hard Delete)",
        description = "Elimina permanentemente un horario laboral. Solo el proveedor dueño del empleado puede eliminarlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Work schedule deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Work schedule not found")
    })
    public ResponseEntity<Void> deleteWorkSchedule(
            @Parameter(description = "ID del horario laboral") @PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        workScheduleService.deleteWorkSchedule(id, providerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Get work schedule by ID",
        description = "Obtiene un horario laboral específico por su ID. Solo el proveedor dueño del empleado puede verlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Work schedule found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Work schedule not found")
    })
    public ResponseEntity<EntityModel<WorkScheduleResponseDTO>> getWorkScheduleById(
            @Parameter(description = "ID del horario laboral") @PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        WorkScheduleResponseDTO dto = workScheduleService.getWorkScheduleById(id, providerId);
        EntityModel<WorkScheduleResponseDTO> entityModel = EntityModel.of(dto,
            linkTo(methodOn(WorkScheduleController.class).getWorkScheduleById(id)).withSelfRel());
        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Get all work schedules by employee",
        description = "Obtiene todos los horarios laborales de un empleado (activos e inactivos). Solo el proveedor dueño puede verlos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Work schedules found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<WorkScheduleResponseDTO>>> getWorkSchedulesByEmployee(
            @Parameter(description = "ID del empleado") @PathVariable UUID employeeId) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        List<WorkScheduleResponseDTO> dtos = workScheduleService.getWorkSchedulesByEmployee(employeeId, providerId);
        List<EntityModel<WorkScheduleResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(WorkScheduleController.class).getWorkScheduleById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(WorkScheduleController.class).getWorkSchedulesByEmployee(employeeId)).withSelfRel()));
    }

    @GetMapping("/employee/{employeeId}/active")
    @Operation(
        summary = "Get active work schedules by employee",
        description = "Obtiene los horarios laborales activos de un empleado ordenados por día y hora. Disponible para cualquier usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active work schedules found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<WorkScheduleResponseDTO>>> getActiveWorkSchedulesByEmployee(
            @Parameter(description = "ID del empleado") @PathVariable UUID employeeId) {
        List<WorkScheduleResponseDTO> dtos = workScheduleService.getActiveWorkSchedulesByEmployee(employeeId);
        List<EntityModel<WorkScheduleResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(WorkScheduleController.class).getWorkScheduleById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(WorkScheduleController.class).getActiveWorkSchedulesByEmployee(employeeId)).withSelfRel()));
    }

    @GetMapping("/employee/{employeeId}/public")
    @Operation(
        summary = "Get work schedules by employee (Public)",
        description = "Obtiene los horarios laborales activos de un empleado para visualización pública. Disponible para cualquier usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Work schedules found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<WorkScheduleResponseDTO>>> getWorkSchedulesByEmployeePublic(
            @Parameter(description = "ID del empleado") @PathVariable UUID employeeId) {
        List<WorkScheduleResponseDTO> dtos = workScheduleService.getWorkSchedulesByEmployeePublic(employeeId);
        List<EntityModel<WorkScheduleResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(WorkScheduleController.class).getWorkScheduleById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(WorkScheduleController.class).getWorkSchedulesByEmployeePublic(employeeId)).withSelfRel()));
    }
}