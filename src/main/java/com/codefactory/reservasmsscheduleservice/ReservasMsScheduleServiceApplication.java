package com.codefactory.reservasmsscheduleservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT token obtenido del endpoint /api/auth/login. Usar el campo 'accessToken' de la respuesta."
)
@OpenAPIDefinition(
    info = @Info(
        title = "Plataforma de Reservas de Servicios - MS-Schedule-Service",
        description = "Microservicio de gestión de empleados y horarios para la plataforma de reservas de servicios. " +
                     "Proporciona funcionalidades de CRUD de empleados, gestión de horarios y disponibilidad.",
        version = "1.0.0",
        contact = @Contact(
            name = "Equipo EAV04",
            email = "isabela.bedoya@udea.edu.co",
            url = "https://github.com/Isa-Bedoya-UdeA/Reservas-MS-Schedule-Service"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "Servidor de Desarrollo",
            url = "http://localhost:8083"
        ),
        @Server(
            description = "Servidor de Producción",
            url = "https://reservas-ms-schedule-service.onrender.com"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
public class ReservasMsScheduleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasMsScheduleServiceApplication.class, args);
    }

}
