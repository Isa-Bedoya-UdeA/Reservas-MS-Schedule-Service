package com.codefactory.reservasmsscheduleservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a service with its associated employees.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceWithEmployeesResponseDTO extends RepresentationModel<ServiceWithEmployeesResponseDTO> {

    private UUID idServicio;
    private UUID idProveedor;
    private String nombreServicio;
    private Integer duracionMinutos;
    private BigDecimal precio;
    private String descripcion;
    private Boolean activo;
    private Integer capacidadMaxima;
    private List<EmployeeResponseDTO> employees;
    private LocalDateTime createdAt;
}
