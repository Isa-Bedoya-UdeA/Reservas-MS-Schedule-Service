package com.codefactory.reservasmsscheduleservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO extends RepresentationModel<EmployeeResponseDTO> {
    private UUID id;
    private UUID providerId;
    private String fullName;
    private String phone;
    private Boolean active;
    private LocalDateTime hireDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
