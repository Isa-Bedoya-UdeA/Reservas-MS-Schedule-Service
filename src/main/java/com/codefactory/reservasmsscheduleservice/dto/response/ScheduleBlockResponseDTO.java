package com.codefactory.reservasmsscheduleservice.dto.response;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBlockResponseDTO extends RepresentationModel<ScheduleBlockResponseDTO> {
    
    private UUID id;
    private UUID employeeId;
    private UUID reservationId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String blockType;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
