# HATEOAS — Hypermedia as the Engine of Application State

## Overview

Este microservicio implementa HATEOAS (parte de Spring HATEOAS) en todas sus respuestas REST, proporcionando enlaces de navegación (`_links`) que permiten a los clientes descubrir y navegar recursos relacionados sin necesidad de conocer URLs beforehand.

## Implementation

### DTOs

Los DTOs de respuesta extienden `RepresentationModel<T>` de Spring HATEOAS en lugar de usar solo `@Data` de Lombok:

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class SomeResponseDTO extends RepresentationModel<SomeResponseDTO> {
    private UUID id;
    private String name;
    // ... campos existentes
}
```

La clase base `RepresentationModel` provee el campo `_links` en la serialización JSON.

### Controllers

Los controllers envuelven las respuestas en `EntityModel<T>` y `CollectionModel<EntityModel<T>>`:

```java
// Endpoint individual
EntityModel<SomeResponseDTO> entityModel = EntityModel.of(dto,
    linkTo(methodOn(ThisController.class).getById(id)).withSelfRel(),
    linkTo(methodOn(ThisController.class).listAll()).withRel("all-items"));
return ResponseEntity.ok(entityModel);

// Endpoint de lista
List<EntityModel<SomeResponseDTO>> models = dtos.stream()
    .map(dto -> EntityModel.of(dto,
        linkTo(methodOn(ThisController.class).getById(dto.getId())).withSelfRel()))
    .collect(Collectors.toList());
return ResponseEntity.ok(CollectionModel.of(models,
    linkTo(methodOn(ThisController.class).listAll()).withSelfRel()));
```

## Example Response

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Nombre del recurso",
  "_links": {
    "self": { "href": "https://api.example.com/resource/123e4567-e89b-12d3-a456-426614174000" },
    "all-items": { "href": "https://api.example.com/resource" }
  }
}
```

## Endpoints with HATEOAS

### Recursos principales

| Método | Endpoint | Enlaces |
|--------|----------|---------|
| GET | `/{id}` | `self`, colección relacionada |
| POST | `/{id}` | `self`, colección |
| PUT | `/{id}` | `self` |
| GET | Colección | `self`, cada item con `self` |
| DELETE / PATCH | `/{id}/action` | Sin body — no aplica |

## Dependency

La dependencia `spring-boot-starter-hateoas` ya está incluida en `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```
