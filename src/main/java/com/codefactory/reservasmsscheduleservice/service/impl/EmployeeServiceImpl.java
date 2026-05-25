package com.codefactory.reservasmsscheduleservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.UpdateEmployeeRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.exception.BusinessException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.mapper.EmployeeMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
import com.codefactory.reservasmsscheduleservice.service.EmployeeService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(CreateEmployeeRequestDTO request, UUID providerIdFromJWT) {
        Employee employee = employeeMapper.toEntity(request);
        employee.setProviderId(providerIdFromJWT);
        employee.setActive(true);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(UUID id, UpdateEmployeeRequestDTO request, UUID providerIdFromJWT) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Validate ownership
        if (!employee.getProviderId().equals(providerIdFromJWT)) {
            throw new AccessDeniedException("No tienes permisos para modificar este empleado");
        }

        employeeMapper.updateEntityFromDto(request, employee);
        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(UUID id, UUID providerIdFromJWT) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Validate ownership
        if (!employee.getProviderId().equals(providerIdFromJWT)) {
            throw new AccessDeniedException("No tienes permisos para eliminar este empleado");
        }

        employeeRepository.delete(employee);
    }

    @Override
    @Transactional
    public void deactivateEmployee(UUID id, UUID providerIdFromJWT) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Validate ownership
        if (!employee.getProviderId().equals(providerIdFromJWT)) {
            throw new AccessDeniedException("No tienes permisos para desactivar este empleado");
        }

        // Validate not already inactive
        if (!employee.getActive()) {
            throw new BusinessException("El empleado ya está inactivo");
        }

        employee.setActive(false);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void activateEmployee(UUID id, UUID providerIdFromJWT) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        // Validate ownership
        if (!employee.getProviderId().equals(providerIdFromJWT)) {
            throw new AccessDeniedException("No tienes permisos para activar este empleado");
        }

        // Validate not already active
        if (employee.getActive()) {
            throw new BusinessException("El empleado ya está activo");
        }

        employee.setActive(true);
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(UUID id, UUID providerIdFromJWT) {
        Employee employee = employeeRepository.findByIdAndProviderId(id, providerIdFromJWT)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return employeeMapper.toDto(employee);
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByProvider(UUID providerId, UUID providerIdFromJWT) {
        // Validate that the requesting provider is the same as the one being queried
        if (!providerId.equals(providerIdFromJWT)) {
            throw new AccessDeniedException("No tienes permisos para ver empleados de otro proveedor");
        }

        return employeeRepository.findByProviderId(providerId).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployeesByProvider(UUID providerId, UUID providerIdFromJWT) {
        // Validate that the requesting provider is the same as the one being queried
        if (!providerId.equals(providerIdFromJWT)) {
            throw new AccessDeniedException("No tienes permisos para ver empleados de otro proveedor");
        }

        return employeeRepository.findByProviderIdAndActiveTrue(providerId).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployees() {
        return employeeRepository.findByActiveTrue().stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    public boolean isEmployeeActive(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getActive)
                .orElse(false);
    }

    @Override
    public UUID getEmployeeProviderId(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getProviderId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
    }

    @Override
    public EmployeeResponseDTO getEmployeeByIdPublic(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        return employeeMapper.toDto(employee);
    }
}
