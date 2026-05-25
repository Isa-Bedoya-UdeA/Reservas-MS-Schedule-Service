package com.codefactory.reservasmsscheduleservice.service;

import java.util.List;
import java.util.UUID;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(CreateEmployeeRequestDTO request, UUID providerIdFromJWT);
    EmployeeResponseDTO updateEmployee(UUID id, UpdateEmployeeRequestDTO request, UUID providerIdFromJWT);
    void deleteEmployee(UUID id, UUID providerIdFromJWT);
    void deactivateEmployee(UUID id, UUID providerIdFromJWT);
    void activateEmployee(UUID id, UUID providerIdFromJWT);
    EmployeeResponseDTO getEmployeeById(UUID id, UUID providerIdFromJWT);
    List<EmployeeResponseDTO> getEmployeesByProvider(UUID providerId, UUID providerIdFromJWT);
    List<EmployeeResponseDTO> getActiveEmployeesByProvider(UUID providerId, UUID providerIdFromJWT);
    List<EmployeeResponseDTO> getActiveEmployees();

    // Methods for Reservation Service integration
    boolean isEmployeeActive(UUID employeeId);
    UUID getEmployeeProviderId(UUID employeeId);
    EmployeeResponseDTO getEmployeeByIdPublic(UUID employeeId);
}
