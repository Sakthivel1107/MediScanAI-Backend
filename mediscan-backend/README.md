# MediScan AI – Backend

Spring Boot backend for **MediScan AI – Intelligent Medicine Strip Reader and Assistant**.

The backend lets a Flutter mobile app register/login users, upload a photo of a medicine
strip, run OCR + AI identification on it, and return structured medicine information,
plus manage scan history and a medicine catalog.

> **Medical safety notice:** MediScan AI is an information-assistance tool. It does **not**
> provide medical diagnoses or prescriptions. Every scan response includes a disclaimer:
> *"This information is extracted from the medicine package and provided for informational
> purposes only. Verify medicine details with a pharmacist or healthcare professional."*
> Fields the AI cannot confidently determine are returned as `null`/`"Unknown"` rather than
> invented.

---

## 1. Features

- User registration & login with JWT authentication (stateless, BCrypt password hashing)
- Medicine strip image upload (multipart/form-data) with type/size validation
- OCR text extraction (Tesseract via Tess4J), abstracted behind `OcrService`
- AI/LLM medicine identification, abstracted behind `AiService`, strict-JSON parsing only
- Scan history: create, list (paginated), get by id, delete — scoped to the owning user
- Medicine catalog: list, get, search, create, update, delete
- Global exception handling with a consistent JSON error envelope, no leaked internals
- Swagger / OpenAPI docs with JWT Bearer auth support
- CORS configured via environment variable, safe for LAN-based Flutter development
- Dockerfile + docker-compose (backend + MongoDB)
- Unit tests (JUnit 5 + Mockito) for auth, scan, medicine, user, JWT, validation, and
  global exception handling

## 2. Technology Stack

Java 21 · Spring Boot 3.3 · Maven · Spring Web · Spring Data MongoDB · Spring Security ·
JWT (jjwt) · Jakarta Bean Validation · Lombok · Jackson · Spring WebClient ·
Tess4J (Tesseract OCR wrapper) · springdoc-openapi (Swagger) · spring-dotenv ·
JUnit 5 · Mockito

Database: **MongoDB**

## 3. Folder Structure

```
mediscan-backend/
├── src/
│   ├── main/
│   │   ├── java/com/mediscan/backend/
│   │   │   ├── MediScanApplication.java
│   │   │   ├── config/          # SecurityConfig, MongoConfig, OpenApiConfig, WebConfig, CorsProperties
│   │   │   ├── controller/      # AuthController, UserController, ScanController, MedicineController, HealthController
│   │   │   ├── service/         # Interfaces (Auth, User, Scan, Medicine, Ocr, Ai, Image, Jwt)
│   │   │   │   └── impl/        # Implementations
│   │   │   ├── repository/      # UserRepository, ScanRepository, MedicineRepository
│   │   │   ├── model/           # User, Scan, Medicine (MongoDB documents)
│   │   │   ├── dto/             # auth/, scan/, medicine/, user/, common/
│   │   │   ├── security/        # JwtAuthenticationFilter, CustomUserDetailsService, CustomUserDetails
│   │   │   ├── exception/       # GlobalExceptionHandler + custom exceptions
│   │   │   ├── util/            # JwtUtil, FileUtil, ValidationUtil
│   │   │   └── enums/           # AuthProvider, ScanStatus
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/java/com/mediscan/backend/
│       ├── controller/
│       ├── service/
│       ├── dto/
│       ├── exception/
│       └── util/
├── pom.xml
├── README.md
├── .gitignore
├── .env.example
├── Dockerfile
└── docker-compose.yml
```

## 4. Requirements

- **Java 21 JDK**
- **Maven 3.9+**
- **MongoDB** (local install, Docker, or MongoDB Atlas)
- **Tesseract OCR** installed on the host (or use the provided Docker image, which
  installs it automatically)
- An AI/LLM API key (this project's `AiService` calls the Anthropic Messages API by
  default; swap the URL/payload in `AiServiceImpl` if you use a different provider)

## 5. Environment Variables

Copy `.env.example` to `.env` and fill in real values. **Never commit `.env`.**

| Variable | Description | Example |
|---|---|---|
| `PORT` | Server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/mediscan` |
| `JWT_SECRET` | Long random secret for signing JWTs | *(generate your own)* |
| `JWT_EXPIRATION` | Token TTL in ms | `86400000` (24h) |
| `AI_API_KEY` | API key for the AI/LLM service | *(your key)* |
| `AI_API_URL` | AI service endpoint | `https://api.anthropic.com/v1/messages` |
| `AI_API_MODEL` | Model name | `claude-sonnet-4-6` |
| `TESSERACT_PATH` | Path to the directory **containing** `tessdata` | see §7 below |
| `TESSERACT_LANGUAGE` | OCR language | `eng` |
| `UPLOAD_DIR` | Local directory for stored images | `./uploads` |
| `MAX_FILE_SIZE` / `MAX_REQUEST_SIZE` | Upload limits | `10MB` / `12MB` |
| `ALLOWED_ORIGINS` | Comma-separated CORS origins | `http://192.168.1.100:8080` |
| `LOG_LEVEL` | App log level | `INFO` |

`spring-dotenv` automatically loads `.env` on startup, so you don't need to `export`
these manually in local development (though you still can).

## 6. MongoDB Setup

**Option A — Local install:** install MongoDB Community Server, then it will be
reachable at `mongodb://localhost:27017` by default.

**Option B — Docker (standalone):**
```bash
docker run -d --name mediscan-mongo -p 27017:27017 mongo:7
```

**Option C — Docker Compose (recommended, brings up backend + Mongo together):**
see §11.

**Option D — MongoDB Atlas (cloud):** create a free cluster and set
`MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>.mongodb.net/mediscan`.

Collections (`users`, `scans`, `medicines`) and indexes are created automatically on
first use via Spring Data MongoDB (`spring.data.mongodb.auto-index-creation=true`).

## 7. Tesseract Installation / Setup

The `OcrService` uses [Tess4J](http://tess4j.sourceforge.net/), a Java wrapper around
the native Tesseract OCR engine. **You need Tesseract installed on the machine running
the backend** (not required if you only use the Docker image, which installs it for you).

- **Ubuntu/Debian:** `sudo apt-get install tesseract-ocr`
- **macOS (Homebrew):** `brew install tesseract`
- **Windows:** download the installer from the
  [UB-Mannheim Tesseract build](https://github.com/UB-Mannheim/tesseract/wiki)

Set `TESSERACT_PATH` to the directory **containing** the `tessdata` folder (not the
`tesseract` executable itself), e.g.:
- Linux: `/usr/share/tesseract-ocr/5/tessdata`
- macOS (brew, Apple Silicon): `/opt/homebrew/share/tessdata`
- Windows: `C:/Program Files/Tesseract-OCR/tessdata`

If left blank, Tess4J falls back to its bundled default search path, which may not
find your installation — setting it explicitly is recommended.

## 8. AI API Configuration

Set `AI_API_KEY`, `AI_API_URL`, and `AI_API_MODEL` in `.env`. `AiServiceImpl` sends the
OCR-extracted text to the configured endpoint and requires the model to respond with
**strict JSON only** (medicine name, generic name, purpose, dosage, expiry date,
warnings, manufacturer, batch number, confidence). Unparseable or unavailable responses
throw a handled `AiServiceException`, surfaced to the client as HTTP 503 — the app never
crashes or fabricates medicine data on AI failure.

## 9. How to Run Locally

```bash
# 1. Copy and fill in environment variables
cp .env.example .env
# edit .env with your MongoDB URI, JWT secret, AI API key, Tesseract path...

# 2. Make sure MongoDB is running (see §6) and Tesseract is installed (see §7)

# 3. Build and run
mvn clean install
mvn spring-boot:run

# The API is now available at http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## 10. How to Run Tests

```bash
mvn test
```

This runs the JUnit 5 / Mockito unit test suite covering registration, login, JWT
generation/validation, scan processing (happy path + AI failure path), scan history,
per-user authorization on scans, medicine CRUD, DTO validation, and the global
exception handler's HTTP status/error-envelope mapping.

> Note: `OcrServiceImplTest` exercises the OCR error path and requires a working
> Tesseract installation to run (see §7) — like `OcrService` itself in production.

## 11. How to Run with Docker

```bash
cp .env.example .env
# edit .env — at minimum set JWT_SECRET and AI_API_KEY

docker compose up --build
```

This starts two containers:
- `mediscan-mongodb` — MongoDB 7, with a named volume for persistence
- `mediscan-backend` — the Spring Boot app (Tesseract OCR is installed inside the image)

The API will be available at `http://localhost:8080` (or your host's LAN IP — see §12).

To stop: `docker compose down` (add `-v` to also remove the Mongo data volume).

## 12. Flutter Integration

The Flutter app and this backend typically run on **different devices** (phone vs.
laptop), so the base URL matters:

| Scenario | Base URL to use in Flutter |
|---|---|
| Android **physical phone** + backend on laptop | `http://<laptop-LAN-IPv4>:8080` |
| Android **emulator** + backend on host machine | `http://10.0.2.2:8080` |
| iOS simulator + backend on host machine | `http://localhost:8080` |

**`10.0.2.2` is a special alias only recognized by the Android Emulator** — it is
**not** valid for a physical Android phone.

### Steps to connect a physical Android phone to your laptop's backend

1. **Find your laptop's IPv4 address:**
   - Windows: `ipconfig` → look for "IPv4 Address" under your active Wi-Fi adapter
     (e.g. `192.168.1.100`)
   - macOS/Linux: `ifconfig` or `ip addr` → look under your Wi-Fi interface (e.g. `en0`)
2. **Start the Spring Boot backend** (`mvn spring-boot:run` or Docker Compose).
3. **Allow the port through the firewall** (Windows example):
   - Windows Defender Firewall → Advanced settings → Inbound Rules → New Rule →
     Port → TCP → `8080` → Allow the connection → apply to all profiles.
4. **Connect the Android phone and laptop to the same Wi-Fi network.**
5. **Configure the Flutter base URL**, e.g.:
   ```dart
   const String baseUrl = "http://192.168.1.100:8080";
   ```
6. Set `ALLOWED_ORIGINS` in `.env` to include the origin your Flutter web/dev tooling
   uses, if applicable (native mobile HTTP calls aren't subject to CORS, but this
   matters for Flutter Web).

## 13. API Endpoints

Base URL: `http://<host>:8080`

### Auth (public)

**POST `/api/auth/register`**
- Headers: `Content-Type: application/json`
- Body:
  ```json
  { "name": "Jane Doe", "email": "jane@example.com", "password": "password123" }
  ```
- Response `201`:
  ```json
  {
    "success": true,
    "message": "User registered successfully",
    "data": {
      "token": "eyJhbGciOi...",
      "tokenType": "Bearer",
      "expiresIn": 86400000,
      "user": { "id": "...", "name": "Jane Doe", "email": "jane@example.com", "provider": "LOCAL", "createdAt": "..." }
    },
    "timestamp": "..."
  }
  ```
- Errors: `400` (validation), `409` (email already exists)

**POST `/api/auth/login`**
- Body: `{ "email": "jane@example.com", "password": "password123" }`
- Response `200`: same shape as register
- Errors: `400` (validation), `401` (invalid credentials)

### User (requires `Authorization: Bearer <token>`)

**GET `/api/users/me`** → current user's profile. Errors: `401`.

### Scan (requires auth)

**POST `/api/scans`**
- Headers: `Authorization: Bearer <token>`, `Content-Type: multipart/form-data`
- Body: form field `file` = image (jpeg/png/webp), optional field `note`
- Response `201`:
  ```json
  {
    "success": true,
    "message": "Scan completed successfully",
    "data": {
      "id": "...", "medicineName": "Paracetamol", "genericName": "Acetaminophen",
      "purpose": "Pain relief", "dosage": "500mg", "expiryDate": "12/2027",
      "warnings": ["Do not exceed 4g per day"], "manufacturer": "ACME Pharma",
      "batchNumber": "B12345", "extractedText": "...", "confidence": 0.9,
      "status": "COMPLETED", "createdAt": "...",
      "disclaimer": "This information is extracted from the medicine package and provided for informational purposes only. Verify medicine details with a pharmacist or healthcare professional."
    },
    "timestamp": "..."
  }
  ```
- Errors: `400` (invalid/missing file), `413` (file too large), `422` (OCR failed),
  `503` (AI service unavailable)

**GET `/api/scans?page=0&size=20`** → paginated scan history for the authenticated user.

**GET `/api/scans/{id}`** → single scan (only if owned by the caller). Errors: `404`.

**DELETE `/api/scans/{id}`** → deletes the scan (only if owned by the caller). Errors: `404`.

### Medicine (requires auth)

**GET `/api/medicines?page=0&size=20`** — list catalog entries.

**GET `/api/medicines/{id}`** — get one. Errors: `404`.

**GET `/api/medicines/search?name=paracetamol&page=0&size=20`** — case-insensitive search.

**POST `/api/medicines`** — create. Body:
```json
{ "name": "Ibuprofen", "genericName": "Ibuprofen", "purpose": "Pain/inflammation relief",
  "dosage": "200mg", "warnings": ["Avoid on empty stomach"], "sideEffects": ["Nausea"],
  "manufacturer": "ACME Pharma" }
```

**PUT `/api/medicines/{id}`** — update (same body shape).

**DELETE `/api/medicines/{id}`** — delete. Errors: `404`.

### Health (public)

**GET `/api/health`** → `{ "status": "UP", "service": "MediScan AI Backend" }`

## 14. Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

Click **Authorize** in Swagger UI and paste a JWT (just the token, no `Bearer ` prefix
needed — springdoc adds it) to call protected endpoints interactively.

## 15. Error Response Format

All errors follow this envelope:
```json
{
  "success": false,
  "message": "Invalid image file",
  "errorCode": "INVALID_FILE",
  "timestamp": "2026-09-01T10:00:00Z"
}
```

Status codes used: `400`, `401`, `403`, `404`, `409`, `413`, `422` (OCR), `503` (AI
unavailable), `500`.

## 16. Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| App fails to start with a Mongo connection error | Check `MONGODB_URI`; confirm MongoDB is running and reachable |
| `401 Unauthorized` on every request | Missing/expired/invalid `Authorization: Bearer <token>` header |
| `422` on scan upload | Tesseract couldn't read the image — check `TESSERACT_PATH`, and that the native Tesseract binary is installed |
| `503` on scan upload | `AI_API_KEY`/`AI_API_URL` misconfigured, network issue, or the AI provider is down |
| Flutter app on a physical phone can't reach the backend | Don't use `localhost` or `10.0.2.2` on a physical device — use the laptop's LAN IPv4 (§12), confirm both devices are on the same Wi-Fi, and check firewall rules |
| CORS errors from a Flutter Web client | Add the dev server's origin to `ALLOWED_ORIGINS` |
| `413 Payload Too Large` | Image exceeds `MAX_FILE_SIZE`/`MAX_REQUEST_SIZE` — resize the image or raise the limit |
| Build fails resolving dependencies | Ensure the build machine has internet access to Maven Central (this cannot be verified inside a network-restricted sandbox — see note below) |

> **Build verification note:** this project was generated and code-reviewed in an
> environment without access to Maven Central, so `mvn compile`/`mvn test` could not be
> executed here to produce a passing build log. Run `mvn clean install` on a machine
> with normal internet access to fetch dependencies and verify the build before deploying.

---

Built for the MediScan AI Flutter mobile app.
