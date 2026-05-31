package com.codefactory.reservasmsscheduleservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.codefactory.reservasmsscheduleservice.dto.request.CheckAvailabilityRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.CreateReservationBlockRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.CreateScheduleBlockRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.GetScheduleBlocksByDateRangeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ScheduleBlockResponseDTO;
import com.codefactory.reservasmsscheduleservice.service.ScheduleBlockService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/schedule/schedule-blocks")
@RequiredArgsConstructor
@Tag(name = "Schedule Block Management", description = "API para gestionar bloqueos de horario por fecha específica")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleBlockController {

    private final ScheduleBlockService scheduleBlockService;

    @PostMapping
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Create schedule block",
        description = "Crea un nuevo bloqueo de horario por fecha específica. Solo el proveedor dueño del empleado puede crear bloqueos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Schedule block created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Schedule block conflicts with existing block or employee doesn't work during this time")
    })
    public ResponseEntity<EntityModel<ScheduleBlockResponseDTO>> createScheduleBlock(
            @Valid @RequestBody CreateScheduleBlockRequestDTO request) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        ScheduleBlockResponseDTO dto = scheduleBlockService.createScheduleBlock(request, providerId);
        EntityModel<ScheduleBlockResponseDTO> entityModel = EntityModel.of(dto,
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlockById(dto.getId())).withSelfRel(),
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlocksByEmployee(dto.getEmployeeId())).withRel("employee-blocks"));
        return ResponseEntity.status(201).body(entityModel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Delete schedule block (Soft Delete)",
        description = "Desactiva un bloqueo de horario (soft delete). Solo el proveedor dueño del empleado puede eliminarlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Schedule block deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Schedule block not found")
    })
    public ResponseEntity<Void> deleteScheduleBlock(
            @Parameter(description = "ID del bloqueo de horario") @PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        scheduleBlockService.deleteScheduleBlock(id, providerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Get schedule block by ID",
        description = "Obtiene un bloqueo de horario específico por su ID. Solo el proveedor dueño del empleado puede verlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schedule block found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Schedule block not found")
    })
    public ResponseEntity<EntityModel<ScheduleBlockResponseDTO>> getScheduleBlockById(
            @Parameter(description = "ID del bloqueo de horario") @PathVariable UUID id) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        ScheduleBlockResponseDTO dto = scheduleBlockService.getScheduleBlockById(id, providerId);
        EntityModel<ScheduleBlockResponseDTO> entityModel = EntityModel.of(dto,
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlockById(id)).withSelfRel());
        return ResponseEntity.ok(entityModel);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('PROVEEDOR')")
    @Operation(
        summary = "Get all schedule blocks by employee",
        description = "Obtiene todos los bloqueos de horario activos de un empleado. Solo el proveedor dueño puede verlos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schedule blocks found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Does not have PROVEEDOR role or not the owner of the employee"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<ScheduleBlockResponseDTO>>> getScheduleBlocksByEmployee(
            @Parameter(description = "ID del empleado") @PathVariable UUID employeeId) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID providerId = UUID.fromString(userIdStr);
        List<ScheduleBlockResponseDTO> dtos = scheduleBlockService.getScheduleBlocksByEmployee(employeeId, providerId);
        List<EntityModel<ScheduleBlockResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(ScheduleBlockController.class).getScheduleBlockById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlocksByEmployee(employeeId)).withSelfRel()));
    }

    @GetMapping("/employee/{employeeId}/public")
    @Operation(
        summary = "Get schedule blocks by employee (Public)",
        description = "Obtiene los bloqueos de horario activos de un empleado para visualización pública. Disponible para cualquier usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schedule blocks found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<ScheduleBlockResponseDTO>>> getScheduleBlocksByEmployeePublic(
            @Parameter(description = "ID del empleado") @PathVariable UUID employeeId) {
        List<ScheduleBlockResponseDTO> dtos = scheduleBlockService.getScheduleBlocksByEmployeePublic(employeeId);
        List<EntityModel<ScheduleBlockResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(ScheduleBlockController.class).getScheduleBlockById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlocksByEmployeePublic(employeeId)).withSelfRel()));
    }

    @PostMapping("/date-range")
    @Operation(
        summary = "Get schedule blocks by employee and date range",
        description = "Obtiene los bloqueos de horario de un empleado en un rango de fechas específico. Disponible para cualquier usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schedule blocks found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<ScheduleBlockResponseDTO>>> getScheduleBlocksByEmployeeAndDateRange(
            @Valid @RequestBody GetScheduleBlocksByDateRangeRequestDTO request) {
        List<ScheduleBlockResponseDTO> dtos = scheduleBlockService.getScheduleBlocksByEmployeeAndDateRange(
            request.getEmployeeId(), 
            request.getStartDate(), 
            request.getEndDate()
        );
        List<EntityModel<ScheduleBlockResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(ScheduleBlockController.class).getScheduleBlockById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlocksByEmployeeAndDateRange(null)).withSelfRel()));
    }

    @GetMapping("/employee/{employeeId}/date")
    @Operation(
        summary = "Get schedule blocks by employee and date",
        description = "Obtiene los bloqueos de horario de un empleado para una fecha específica. Disponible para cualquier usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Schedule blocks found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<CollectionModel<EntityModel<ScheduleBlockResponseDTO>>> getScheduleBlocksByEmployeeAndDate(
            @Parameter(description = "ID del empleado") @PathVariable UUID employeeId,
            @Parameter(description = "Fecha específica") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleBlockResponseDTO> dtos = scheduleBlockService.getScheduleBlocksByEmployeeAndDate(employeeId, date);
        List<EntityModel<ScheduleBlockResponseDTO>> models = dtos.stream()
            .map(dto -> EntityModel.of(dto,
                linkTo(methodOn(ScheduleBlockController.class).getScheduleBlockById(dto.getId())).withSelfRel()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models,
            linkTo(methodOn(ScheduleBlockController.class).getScheduleBlocksByEmployeeAndDate(employeeId, date)).withSelfRel()));
    }

    @PostMapping("/reservation")
    @Operation(
        summary = "Create reservation block (Internal)",
        description = "Crea un bloqueo de horario para una reserva. Usado internamente por el microservicio de reservas. Requiere autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reservation block created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or employee doesn't work during this time"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Schedule block conflicts with existing block")
    })
    public ResponseEntity<Void> createReservationBlock(
            @Valid @RequestBody CreateReservationBlockRequestDTO request) {
        scheduleBlockService.createReservationBlock(
            request.getEmployeeId(), 
            request.getReservationId(), 
            request.getDate(), 
            request.getStartTime(), 
            request.getEndTime()
        );
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/reservation/{reservationId}")
    @Operation(
        summary = "Cancel reservation block (Internal)",
        description = "Cancela un bloqueo de horario de una reserva. Usado internamente por el microservicio de reservas. Requiere autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Reservation block cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Void> cancelReservationBlock(
            @Parameter(description = "ID de la reserva") @PathVariable UUID reservationId) {
        scheduleBlockService.cancelReservationBlock(reservationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-availability")
    @Operation(
        summary = "Check employee availability",
        description = "Verifica si un empleado está disponible en una fecha y hora específicas. Disponible para cualquier usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability checked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Boolean> checkEmployeeAvailability(
            @Valid @RequestBody CheckAvailabilityRequestDTO request) {
        boolean isAvailable = scheduleBlockService.isEmployeeAvailable(
            request.getEmployeeId(), 
            request.getDate(), 
            request.getStartTime(), 
            request.getEndTime()
        );
        return ResponseEntity.ok(isAvailable);
    }
}