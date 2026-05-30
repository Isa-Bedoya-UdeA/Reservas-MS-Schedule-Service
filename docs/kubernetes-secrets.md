# Secrets para Kubernetes - MS-Schedule-Service

Este documento describe las variables necesarias para el archivo `secrets.yaml` requerido para el despliegue en Kubernetes.

## Ubicación del Archivo

El archivo `secrets.yaml` debe estar en la carpeta `kubernetes/` del proyecto:

```
Reservas-MS-Schedule-Service/
└── kubernetes/
    ├── k8s.yaml        # ConfigMap + Deployment + Service
    ├── secrets.yaml    # Secrets (NO subir a Git)
    └── .gitignore      # Ignora secrets.yaml
```

## Variables Requeridas

### 1. Base de Datos (Supabase)

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | URL JDBC de PostgreSQL (Transaction Pooler) |
| `DB_USER` | Usuario de Supabase (`postgres.[PROJECT-REF]`) |
| `DB_PASSWORD` | Contraseña de la base de datos |

### 2. JWT

| Variable | Descripción |
|----------|-------------|
| `JWT_SECRET` | Secreto para validar tokens JWT |

> **IMPORTANTE:** El `JWT_SECRET` debe ser **el mismo valor** que en Auth-Service.

### 3. URLs de Servicios

| Variable | Descripción |
|----------|-------------|
| `SERVICES_AUTH_URL` | URL del Auth Service (`http://auth-service:8081`) |

## Ejemplo de secrets.yaml

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: schedule-service-secrets
  namespace: default
type: Opaque
stringData:
  DB_URL: "jdbc:postgresql://..."
  DB_USER: "postgres.[PROJECT-REF]"
  DB_PASSWORD: "[TU-CONTRASEÑA]"
  JWT_SECRET: "[TU-JWT-SECRET]"
  SERVICES_AUTH_URL: "http://auth-service:8081"
```

## Aplicar en Kubernetes

```bash
kubectl apply -f kubernetes/secrets.yaml
```

## Seguridad

⚠️ **NUNCA** subas el archivo `secrets.yaml` a Git. Ya está incluido en `.gitignore`.
