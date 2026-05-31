# Observabilidad: Kubernetes, ArgoCD, Prometheus y Grafana

Este documento describe cómo desplegar y monitorear el microservicio usando Kubernetes, ArgoCD para GitOps, y Prometheus/Grafana para observabilidad.

---

## 1. Requisitos Previos

- **kubectl** configurado指向 tu cluster
- **Docker** corriendo localmente
- Acceso a **Minikube** o un cluster Kubernetes (v1.25+)
- Repo clonado y cambios en branch `main` subidos a GitHub

---

## 2. Configuración de Secrets

Antes de desplegar, necesitas crear el Secret con las variables de entorno:

```bash
# Aplica el archivo de secrets (ya incluye las variables necesarias)
kubectl apply -f kubernetes/secrets.yaml
```

> **Nota:** Los secrets contienen credenciales sensibles (DB, JWT, email). Nunca commitear el archivo `secrets.yaml` con valores reales a Git. El archivo en el repo usa placeholders que debes reemplazar con tus valores reales antes de aplicar.

---

## 3. Despliegue Manual en Kubernetes

### 3.1 Aplicar configuración

```bash
kubectl apply -f kubernetes/k8s.yaml
```

Esto crea:
- **ConfigMap** `schedule-service-config` — variables de entorno (`SPRING_PROFILE=prod`)
- **Deployment** `schedule-service` — 4 réplicas
- **Service** `schedule-service` — ClusterIP en puerto 8083

### 3.2 Verificar despliegue

```bash
# Ver pods
kubectl get pods -l app=schedule-service

# Ver estado del servicio
kubectl get svc schedule-service

# Ver logs
kubectl logs -l app=schedule-service --tail=50
```

### 3.3 Escalar réplicas

```bash
kubectl scale deployment schedule-service --replicas=4
```

---

## 4. Despliegue con ArgoCD (GitOps)

ArgoCD sincroniza automáticamente los cambios desde GitHub al cluster.

### 4.1 Verificar ArgoCD instalado

```bash
kubectl get pods -n argocd
```

### 4.2 Registrar el repo en ArgoCD

```bash
# Obtener la URL del repo
git remote get-url origin

# En la UI de ArgoCD (http://localhost:8080), ir a:
# Settings > Repositories > Connect Repo
# Usar la URL del remote y credenciales de GitHub
```

### 4.3 Crear Application en ArgoCD

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: schedule-service
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/Isa-Bedoya-UdeA/Reservas-MS-Schedule-Service.git
    targetRevision: main
    path: kubernetes
  destination:
    server: https://kubernetes.default.svc
    namespace: default
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

```bash
kubectl apply -f - <<EOF
# (pegar el YAML de arriba)
EOF
```

### 4.4 Sincronizar desde CLI

```bash
argocd app sync schedule-service
argocd app wait schedule-service
```

---

## 5. Observabilidad con Prometheus y Grafana

### 5.1 Prometheus

El endpoint de métricas está disponible en:

```
http://schedule-service.default.svc.cluster.local:8083/actuator/prometheus
```

**Ver métricas en Prometheus:**

```bash
# Port-forward hacia Prometheus
minikube service --url prometheus -n monitoring

# O acceder directamente si está configurado NodePort
# Puerto: 30900 → http://localhost:30900
```

En la UI de Prometheus ve a **Status > Targets** — deberías ver `schedule-service` como **UP**.

**Queries útiles:**

| Métrica | Query |
|---------|-------|
| Estado del servicio | `up{job="schedule-service"}` |
| Memoria JVM | `jvm_memory_used_bytes{job="schedule-service"}` |
| Tiempo de respuesta | `http_server_requests_seconds_count{job="schedule-service"}` |
| Uso de CPU | `process_cpu_usage{job="schedule-service"}` |

### 5.2 Grafana

**Acceso:**

```bash
minikube service --url grafana -n monitoring

# Credenciales: admin / prom-operator
# Puerto: 30300 → http://localhost:30300
```

**Importar dashboards:**

1. Ve a **Dashboards > Import**
2. Dashboard ID **4701** (JVM Micrometer) o **10280** (Spring Boot)
3. Selecciona "Prometheus" como data source
4. Click Import

**Crear dashboard personalizado:**

1. Ve a **+ > Create > Dashboard**
2. Agrega un **Panel**
3. Query ejemplo: `{job="schedule-service"}`
4. Selecciona las métricas que necesitas visualizar

---

## 6. Verificación Completa

### Checklist de verificación

```bash
# 1. Secret aplicado
kubectl get secret schedule-service-secrets

# 2. Deployment corriendo
kubectl get deployment schedule-service

# 3. 4 pods en estado Running
kubectl get pods -l app=schedule-service

# 4. Service expuesta
kubectl get svc schedule-service

# 5. Logs sin errores críticos
kubectl logs -l app=schedule-service | Select-String ERROR

# 6. Métricas respondiendo (desde un pod de prueba)
kubectl run test --image=curlimages/curl --rm -it --restart=Never -- \
  wget -qO- http://schedule-service:8083/actuator/prometheus | Select-String "^# HELP"
```

---

## 7. Troubleshooting

### El pod no inicia (OOMKilled)

```bash
# Reducir réplicas para liberar memoria
kubectl scale deployment schedule-service --replicas=2
```

### Error de conexión a la base de datos

```bash
# Verificar secrets
kubectl describe secret schedule-service-secrets

# Ver logs de HikariCP
kubectl logs -l app=schedule-service | Select-String HikariPool
```

### Prometheus no scrapea el endpoint

```bash
# Ver targets en Prometheus
kubectl exec -n monitoring prometheus-pod-name -- \
  wget -qO- 'http://localhost:9090/api/v1/targets'

# Verificar que el endpoint responde
kubectl run test --image=curlimages/curl --rm -it --restart=Never -- \
  wget -qO- http://schedule-service:8083/actuator/prometheus
```

---

## 8. Referencias

- [kubernetes/k8s.yaml](kubernetes/k8s.yaml) — Configuración del Deployment
- [kubernetes/secrets.yaml](kubernetes/secrets.md) — Gestión de secrets
- [docs/environment-variables.md](environment-variables.md) — Variables de entorno