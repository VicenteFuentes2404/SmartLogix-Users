<<<<<<< HEAD
# SmartLogix-Users
Servicio de Usuarios del proyecto SmartLogix.
=======
# Users MS

Microservicio de registro, login, perfil, telefono y direcciones de usuarios.

## Ejecutar

```powershell
mvn spring-boot:run
```

Puerto: `8081`

## Pruebas y cobertura

```powershell
mvn test
mvn jacoco:report
mvn clean verify
```

Reporte JaCoCo:

```text
target/site/jacoco/index.html
```

Cobertura verificada: `78,69%`.

## Swagger

```text
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

## DTOs principales

- `RegisterRequest`
- `LoginRequest`
- `AuthResponse`
- `UserProfileResponse`
- `UpdatePhoneRequest`
- `UserAddressRequest`
- `UserAddressResponse`

Los controllers responden DTOs y no exponen `User` ni `UserAddress` directamente.
>>>>>>> 7ee3a60 (Inicializando proyecto)
