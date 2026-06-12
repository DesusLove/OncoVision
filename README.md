<p align="center">
  <h1 align="center">OncoVision</h1>
  <p align="center">Breast cancer detection from histopathology images — deep learning inference, patient management, and a premium clinical dashboard.</p>
</p>

---

## Architecture

```
Histopathology Image
       │
       ▼
┌─────────────────────┐     ┌──────────────────────────┐     ┌──────────┐
│  ML Service (Python) │────▶│  Backend (Java/Spring)    │────▶│  MySQL   │
│  FastAPI + PyTorch   │     │  Patient CRUD + Diagnosis │     │  (or H2) │
└─────────────────────┘     └──────────────────────────┘     └──────────┘
                                     ▲
                                     │
                              ┌──────────────┐
                              │  Frontend     │
                              │  Vue 3 + Vite │
                              └──────────────┘
```

- **ML service** (Python / FastAPI) — serves two ResNet-18 models: a binary detector (benign vs malignant) and an 8-class subtype classifier. Stateless — only does inference, knows nothing about patients.
- **Backend** (Java / Spring Boot + JPA) — patient CRUD, image upload → diagnosis pipeline, record history, dashboard statistics.
- **Frontend** (Vue 3 + Vite) — premium clinical dashboard with dark mode, 6-language i18n, ⌘K command palette, animated statistics, donut chart, toast notifications, and keyboard shortcuts.

## Models

Transfer learning (ResNet-18, ImageNet-pretrained) on the **BreakHis** dataset at 200x magnification, evaluated on a held-out test set. The binary model is threshold-tuned to prioritise **recall** (avoid missed malignancies); the subtype model adds histological detail. The benign/malignant decision is always taken from the binary model.

| Model             | Task                    | Accuracy | Key Metric          |
|-------------------|-------------------------|----------|---------------------|
| Binary detector   | benign vs malignant     | 95.5%    | malignant recall 100% |
| Subtype classifier | 8 histological subtypes | 90.6%    | macro-F1 0.882      |

> Evaluated on an image-level split; a patient-level split is recommended for leakage-free benchmarking. Training notebooks are in `ml/notebooks/`.

## Frontend Features

Sources are under `frontend/src/`:
- `App.vue` — shell, header, command palette, keyboard shortcuts
- `views/` — one component per tab (Dashboard, Patients, Diagnose, Records)
- `components/` — presentational + modal pieces
- `composables/` — i18n, toasts, command palette, api, format helpers

- **Dark mode** — system-aware with manual toggle, persisted to localStorage. *Source: App.vue (`toggleDark`)*
- **6-language i18n** — English, 中文, Русский, Oʻzbek, Қазақша, Türkçe. *Source: composables/i18n.js, composables/useI18n.js*
- **⌘K command palette** — search and trigger any action from the keyboard. *Source: App.vue (`paletteActions`, `onKeyDown`)*
- **Keyboard shortcuts** — ⌘1–4 tab navigation, ⌘D dark mode, Esc to dismiss. *Source: App.vue (`onKeyDown`)*
- **Animated stat counters** — eased count-up on dashboard load. *Source: components/AnimatedCount.vue*
- **SVG donut chart** — subtype distribution with color legend and tooltips. *Source: components/DonutChart.vue*
- **Toast notifications** — slide-in toasts for all CRUD feedback. *Source: composables/useToasts.js, components/ToastContainer.vue*
- **Skeleton loading** — shimmer placeholders during data fetches. *Source: App.vue (`.skeleton` styles)*
- **Sortable tables** — click column headers to sort patients and records. *Source: views/PatientsView.vue, views/RecordsView.vue (`toggleSort`)*
- **Patient view/edit modals** — inline detail view with diagnosis history. *Source: components/PatientViewModal.vue*
- **Delete confirmation** — dialog prevents accidental data loss. *Source: components/DeleteConfirmModal.vue*
- **Image preview** — thumbnail before diagnosis with drag-and-drop. *Source: views/DiagnoseView.vue (drop-zone + preview)*
- **Verify / correct records** — doctors can confirm a diagnosis or submit a correction via `PUT /api/records/{id}/verify`. *Source: components/VerifyRecordModal.vue, views/RecordsView.vue*
- **Print styles** — clean paper output for medical records, with a generated header (tab + date). *Source: App.vue `@media print` block*
- **Responsive** — mobile hamburger nav, adaptive grid layouts. *Source: App.vue `@media (max-width: 760px)`*
- **Professional empty states** — SVG illustrations with helpful CTAs. *Source: views/PatientsView.vue, views/RecordsView.vue*

## Database Schema

```
patients                    diagnostic_records
├── id (PK)                 ├── id (PK)
├── patientId (unique)      ├── patient_id (FK → patients)
├── fullName                ├── testDate
├── gender                  ├── imageFilename
└── passportNumber          ├── binaryLabel
                            ├── binaryProbability
                            ├── subtypeLabel
                            ├── subtypeConfidence
                            ├── verified
                            ├── correctedLabel
                            └── createdAt
```

One-to-many: Patient → DiagnosticRecords (cascade delete).

## API

| Method   | Endpoint                         | Description                              |
|----------|----------------------------------|------------------------------------------|
| `POST`   | `/api/patients`                  | Register a new patient                   |
| `GET`    | `/api/patients?q=&page=&size=`   | Search + paginate patients               |
| `GET`    | `/api/patients/options`          | Slim patient list for the diagnose dropdown (capped at 10 000; `truncated` flag in response) |
| `GET`    | `/api/patients/{id}`             | Get patient by ID                        |
| `PUT`    | `/api/patients/{id}`             | Update patient (returns 409 on uniqueness conflict) |
| `DELETE` | `/api/patients/{id}`             | Delete patient (cascades records)        |
| `POST`   | `/api/patients/{id}/diagnose`    | Upload image → ML predict → save record  |
| `GET`    | `/api/records?label=&from=&to=`  | Filter + paginate diagnostic records     |
| `GET`    | `/api/records/{id}`              | Get a single record                      |
| `PUT`    | `/api/records/{id}/verify`       | Mark a record verified, optionally with a corrected label |
| `DELETE` | `/api/records/{id}`              | Delete a single record                   |
| `GET`    | `/api/patients/{id}/records`     | Records for a specific patient           |
| `GET`    | `/api/stats`                     | Dashboard statistics                     |
| `GET`    | `/h2-console`                    | H2 database browser (dev only)           |

ML service:
| Method | Endpoint      | Description                |
|--------|---------------|----------------------------|
| `POST` | `/predict`    | Image → binary + subtype   |
| `GET`  | `/health`     | Liveness check             |

## Tech Stack

| Layer    | Technology                                               |
|----------|----------------------------------------------------------|
| ML       | Python 3.12, PyTorch, torchvision, FastAPI, Uvicorn      |
| Backend  | Java 17, Spring Boot, Spring Data JPA, Lombok            |
| Frontend | Vue 3, Vite                                              |
| Database | MySQL (prod), H2 in-memory (dev)                         |
| Tools    | uv (Python package manager), Maven Wrapper, npm          |

## Project Structure

```
breast-cancer-binary-multiclass/
├── ml/
│   ├── model/              # Trained .pt weights + metadata JSON
│   │   └── README.md       #   dummy_weights.py generator + training path
│   ├── notebooks/          # Jupyter training notebooks
│   └── service/            # FastAPI inference server
│       ├── app.py
│       └── requirements.txt
├── backend/
│   ├── src/main/java/.../  # Spring Boot application
│   │   ├── controller/     # REST controllers
│   │   │   ├── PatientController.java
│   │   │   ├── RecordController.java      # verify, get, delete, list
│   │   │   ├── DiagnosisController.java   # multipart diagnose only
│   │   │   └── StatsController.java
│   │   ├── service/        # Business logic
│   │   │   ├── PatientService.java
│   │   │   ├── DiagnosisService.java
│   │   │   └── StatsService.java
│   │   ├── entity/         # JPA entities
│   │   │   ├── Patient.java
│   │   │   ├── DiagnosticRecord.java
│   │   │   ├── BinaryLabel.java           # enum
│   │   │   ├── Gender.java                # enum
│   │   │   └── SubtypeLabel.java          # enum
│   │   ├── dto/            # Request/response DTOs
│   │   │   ├── PatientRequest.java
│   │   │   ├── PatientOption.java         # slim dropdown row
│   │   │   ├── DiagnosticRecordView.java  # lowercase wire format
│   │   │   └── DiagnosisResult.java
│   │   ├── repository/     # Spring Data repos
│   │   ├── config/         # CORS, app config
│   │   └── exception/      # GlobalExceptionHandler (RFC 7807)
│   ├── src/main/resources/ # application.properties, application-mysql.properties
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── App.vue                # shell, header, modals, keyboard shortcuts
│   │   ├── main.js                # Vue bootstrap
│   │   ├── views/                 # one file per tab
│   │   │   ├── DashboardView.vue
│   │   │   ├── PatientsView.vue
│   │   │   ├── DiagnoseView.vue
│   │   │   └── RecordsView.vue
│   │   ├── components/            # presentational + modal pieces
│   │   │   ├── AnimatedCount.vue
│   │   │   ├── DonutChart.vue
│   │   │   ├── StatCard.vue
│   │   │   ├── CommandPalette.vue
│   │   │   ├── ToastContainer.vue
│   │   │   ├── DeleteConfirmModal.vue
│   │   │   ├── PatientViewModal.vue
│   │   │   └── VerifyRecordModal.vue
│   │   └── composables/           # state + utility hooks
│   │       ├── i18n.js            #   all 6 languages
│   │       ├── useI18n.js
│   │       ├── useToasts.js
│   │       ├── useCommandPalette.js
│   │       ├── useApi.js
│   │       └── format.js
│   ├── index.html
│   └── package.json
├── .github/workflows/      # ci.yml runs backend, frontend, ml-service jobs
└── README.md
```

## Getting Started

### Prerequisites

- Python 3.12+ (or `uv` for automatic Python version management)
- Java 17+
- Node.js 18+
- MySQL 8+ (optional; H2 is used in dev mode)

### 1. ML Inference Service

```bash
cd ml/service

# Using uv (recommended — auto-manages Python version + deps):
uv run --python 3.12 \
  --with fastapi --with uvicorn --with torch --with torchvision \
  --with pillow --with python-multipart --with "numpy<2" \
  uvicorn app:app --host 0.0.0.0 --port 8000

# Or with pip:
pip install -r requirements.txt
uvicorn app:app --port 8000
```

If you don't have the trained `.pt` weights, generate dummies for end-to-end testing:

```bash
cd ml
uv run --python 3.12 --with torch --with torchvision python model/dummy_weights.py
```

Predictions will be random but the pipeline works. See `ml/model/README.md` for details.

Verify: `curl http://localhost:8000/health` → `{"status":"ok"}`

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend starts on port 8080 with an H2 in-memory database (no MySQL setup needed for dev).

- API: `http://localhost:8080/api/`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:patientdb`
  - User: `sa`, no password

To use MySQL, activate the `mysql` profile:

```bash
SPRING_PROFILES_ACTIVE=mysql \
  DB_URL=jdbc:mysql://localhost:3306/patientdb \
  DB_USER=root DB_PASSWORD=yourpassword \
  ./mvnw spring-boot:run
```

Settings live in `application-mysql.properties`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Opens at `http://localhost:5173`. Vite hot-reloads on changes.

### 4. Full Pipeline Test

```bash
# Create a patient
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{"patientId":"P-001","fullName":"Jane Doe","gender":"FEMALE","passportNumber":"AB123456"}'

# Run diagnosis
curl -X POST "http://localhost:8080/api/patients/1/diagnose" \
  -F "image=@/path/to/histopathology.png" \
  -F "testDate=2026-06-12"
```

## Model Training

Trained weights (`.pt` files) belong in `ml/model/`:

| File                              | Description          |
|-----------------------------------|----------------------|
| `resnet18_breakhis_200x.pt`       | Binary model weights |
| `metadata.json`                   | Classes, threshold   |
| `resnet18_multiclass_200x.pt`     | Subtype model weights|
| `metadata_multiclass_200x.json`   | 8 subtype classes    |

See `ml/model/README.md` for the dummy-weights generator, the training path, and the BreakHis dataset link.

## Configuration

### Backend

`application.properties` defaults to the dev profile (H2 in-memory). For MySQL, activate the `mysql` profile (see step 2 above); settings live in `application-mysql.properties`.

### Frontend

All configuration is in-code. Language preference, theme, and settings persist in `localStorage`.

## Keyboard Shortcuts

| Shortcut     | Action             |
|--------------|--------------------|
| `⌘K`         | Command palette    |
| `⌘1` – `⌘4`  | Switch tabs        |
| `⌘D`         | Toggle dark mode   |
| `Esc`        | Close modal/palette|

## License

This project is provided for educational and research purposes. The BreakHis dataset has its own terms of use.
