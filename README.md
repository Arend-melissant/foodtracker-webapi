# FoodTracker WebAPI

A clean Java WebAPI project designed using **Hexagonal Architecture (Ports & Adapters)** to track meals, food consumption, and adverse reactions (symptoms/allergies) that may occur days later, enabling correlation analysis and trigger detection.

---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.


## 🌟 Key Features & Architecture Standards

1. **Clean Code & Hexagonal Architecture**:
   - **Domain Layer (`domain/`)**: Pure business logic with zero framework dependencies.
   - **Application Layer (`application/`)**: Inbound ports (use cases), Outbound ports (SPIs), and orchestration services.
   - **Infrastructure Layer (`infrastructure/`)**:
     - *Inbound*: REST controllers, DTOs, OpenAPI/Swagger annotations, React UI.
     - *Outbound*: SQLite persistence via Hibernate ORM, internal message bus, Google OAuth2 token verification.
2. **Modern Jakarta EE Ecosystem**:
   - Standardized on modern `jakarta.*` packages: `jakarta.ws.rs.*`, `jakarta.persistence.*`, `jakarta.xml.bind.*`, `jakarta.validation.*`, and `jakarta.inject.*`.
3. **Bi-Directional JSON & XML Support**:
   - All endpoints support content negotiation via `Content-Type` and `Accept` headers (`application/json` and `application/xml`).
   - Strongly-typed request/response DTOs with Jackson and JAXB annotations.
4. **Adverse Effect Lookback & Correlation**:
   - `GET /api/v1/reactions/{id}/suspect-foods?lookbackHours=72`: Analyzes foods eaten in the preceding 1–3 days prior to an adverse reaction.
   - `GET /api/v1/analytics/correlations`: Aggregates and ranks repeat trigger foods across all reported symptoms.
5. **Internal Message Bus**:
   - Decoupled `EventPublisherPort` emitting domain events (`FoodLoggedEvent`, `AdverseReactionLoggedEvent`) asynchronously.
6. **SQLite Storage with Hibernate ORM**:
   - Local relational database file: `foodtracker.db` with automated schema generation and JPA entity mappings.
7. **Google OAuth2 Security**:
   - Validates Google ID tokens against Google JWKS (issuer `accounts.google.com`).
   - Provides a local Dev Login mode (`dev-token:<email>:<name>`) for instant zero-config testing.
8. **Interactive Swagger UI & React Test UI**:
   - **React UI**: Interactive single-page UI at `http://localhost:8080/` to test logins, logging foods, logging reactions, viewing correlations, and toggling JSON/XML.
   - **Swagger UI**: Interactive developer endpoint at `http://localhost:8080/swagger`.
   - **OpenAPI 3.0 Spec**: Available at `http://localhost:8080/openapi.json`.

---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.


## 🚀 Running the Application

### Prerequisites:
- Java 17+
- Maven 3.9+ (Installed at `C:\Tools\maven\apache-maven-3.9.9\bin\mvn.cmd`)

### Start Server via Maven:
```powershell
mvn exec:java
```

### Or Build and Run Executable Fat-JAR:
```powershell
mvn clean package
java -jar target/foodtracker-webapi-1.0.0-SNAPSHOT.jar
```

### Run with Docker / Docker Compose:
To build and run in a lightweight, containerized environment:

```powershell
# Using Docker Compose (recommended - with persistent SQLite volume):
docker compose up --build

# Or using plain Docker:
docker build -t foodtracker-webapi .
docker run -d -p 8080:8080 -v foodtracker-data:/app/data --name foodtracker-webapi foodtracker-webapi
```

Once started:
- **React Test UI**: [http://localhost:8080/](http://localhost:8080/)
- **Swagger / OpenAPI UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)
- **OpenAPI 3.0 JSON**: [http://localhost:8080/openapi.json](http://localhost:8080/openapi.json)
- **REST Endpoints**: [http://localhost:8080/api/v1/](http://localhost:8080/api/v1/)

---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.


## 📡 API Reference

### 1. Authentication (`/api/v1/auth`)
| Method | Path | Description | Protected |
| :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 |
| `POST` | `/api/v1/auth/login` | Authenticate with Google OAuth2 ID Token or Dev Credentials | No |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile | Yes (Bearer) |

### 2. Food Tracking (`/api/v1/foods`)
| Method | Path | Description | Protected |
| :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 |
| `GET` | `/api/v1/foods` | List food entries for current user (optional `start` & `end` query params) | Yes (Bearer) |
| `POST` | `/api/v1/foods` | Log a meal/food (accepts JSON or XML) | Yes (Bearer) |
| `GET` | `/api/v1/foods/{id}` | Get specific food entry | Yes (Bearer) |
| `DELETE` | `/api/v1/foods/{id}` | Delete food entry | Yes (Bearer) |

### 3. Adverse Reactions (`/api/v1/reactions`)
| Method | Path | Description | Protected |
| :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 |
| `GET` | `/api/v1/reactions` | List reported symptoms/reactions | Yes (Bearer) |
| `POST` | `/api/v1/reactions` | Log an adverse symptom (accepts JSON or XML) | Yes (Bearer) |
| `GET` | `/api/v1/reactions/{id}` | Get reaction details | Yes (Bearer) |
| `DELETE` | `/api/v1/reactions/{id}` | Delete reaction entry | Yes (Bearer) |
| `GET` | `/api/v1/reactions/{id}/suspect-foods` | Find foods consumed in prior hours (default: 72h / 3 days) | Yes (Bearer) |

### 4. Correlation Analytics (`/api/v1/analytics`)
| Method | Path | Description | Protected |
| :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 | :---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.
 |
| `GET` | `/api/v1/analytics/correlations` | Aggregated rankings of foods recurring before symptoms | Yes (Bearer) |

---
## 📦 Kubernetes Deployment

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest deploys the `foodtracker-webapi:latest` Docker image, sets environment variables, mounts a hostPath volume at `/app/data` (default `/tmp/foodtracker-data`) for SQLite persistence, and exposes the service via a NodePort (`30080`). For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

### Azure Container Apps (Docker Hub)

You can also deploy the same container to Azure Container Apps using Docker Hub as the image source. The `k8s/deploy-azure.sh` script builds the image, pushes it to Docker Hub, and creates the Azure resources. Run:

```bash
cd k8s
./deploy-azure.sh
```

Ensure you set `DOCKER_USERNAME` and `DOCKER_PASSWORD` in the script. The script does not require an Azure Container Registry; it pulls the image directly from Docker Hub.

The project includes a Kubernetes manifest in the `k8s` folder.

```bash
# Apply the manifest
kubectl apply -f k8s/k8s-deployment.yaml

# Check the pods
kubectl get pods -l app=foodtracker-webapi
```

The manifest:

- Deploys the `foodtracker-webapi:latest` Docker image.
- Sets environment variables `SERVER_HOST`, `SERVER_PORT`, and `DATABASE_URL`.
- Mounts a `hostPath` volume at `/app/data` (default host path `/tmp/foodtracker-data`) for persistent SQLite storage.
- Exposes the service via a NodePort (`30080`) – reachable at `http://localhost:30080`.

**Persistence verification**

1. Create a food entry via the API or UI.
2. Delete the pod (`kubectl delete pod <pod-name>`).
3. Re‑apply the manifest or let the Deployment recreate the pod.
4. The previously stored data remains under the host directory (`/tmp/foodtracker-data/foodtracker.db`).

For local development with Docker Desktop’s Kubernetes, ensure the host path exists:

```bash
mkdir -p /tmp/foodtracker-data
```

You can also adjust the `path` in `k8s/k8s-deployment.yaml` to another directory on your node.


## 🧪 Testing

Run all unit, integration, serialization, and persistence tests:
```powershell
mvn test
```
All 11 test suites verify:
- Domain model immutability & validation rules
- Adverse reaction 72-hour window lookback & correlation service
- Bi-directional JSON & XML serialization (JAXB & Jackson XML)
- Internal asynchronous message bus dispatch
- SQLite schema generation & Hibernate ORM entity persistence

