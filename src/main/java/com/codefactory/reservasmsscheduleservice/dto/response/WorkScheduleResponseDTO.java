package com.codefactory.reservasmsscheduleservice.dto.response;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleResponseDTO extends RepresentationModel<WorkScheduleResponseDTO> {
    
    private UUID id;
    private UUID employeeId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
