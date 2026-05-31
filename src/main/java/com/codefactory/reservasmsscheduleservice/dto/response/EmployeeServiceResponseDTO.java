package com.codefactory.reservasmsscheduleservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing an employee-service association response.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeServiceResponseDTO extends RepresentationModel<EmployeeServiceResponseDTO> {

    private UUID id;
    private UUID employeeId;
    private UUID serviceId;
    private Boolean active;
    private LocalDateTime assignmentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
