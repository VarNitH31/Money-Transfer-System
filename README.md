
# Money Transfer System

A production-grade digital money transfer microservice built progressively aligned with training curriculum.

## 📚 Training Modules

This project is built progressively across 5 training modules:

1. **GIT** - Repository & Version Control
2. **Advanced Java** - Domain Models & Business Logic
3. **Spring Boot** - REST APIs & Backend Services
4. **Angular** - Frontend Application
5. **Snowflake** - Data Analytics & Warehousing

## 🏗️ Architecture
```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   Angular   │─────▶│  Spring Boot │─────▶│    MySQL    │
│   Frontend  │      │   REST API   │      │  Database   │
└─────────────┘      └──────────────┘      └─────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  Snowflake   │
                     │  Analytics   │
                     └──────────────┘
```

## 🛠️ Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Version Control | Git, GitHub | 2.x |
| Language | Java | 17 LTS |
| Framework | Spring Boot | 3.x |
| Frontend | Angular | 18+ |
| Database | MySQL | 8.x |
| Data Warehouse | Snowflake | Cloud |
| Testing | JUnit | 5.x |

## 📂 Project Structure
```
money-transfer-system/
├── backend/              # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/             # Angular application
├── database/             # MySQL scripts
│   ├── schema.sql
│   └── seed-data.sql
├── snowflake/           # Snowflake analytics scripts
├── docs/                # Documentation
└── README.md
```

## 🌿 Branching Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `develop` | Integration branch |
| `feature/domain-models` | Java domain classes |
| `feature/spring-boot-api` | REST API development |
| `feature/angular-ui` | Frontend development |
| `feature/snowflake-analytics` | Analytics setup |

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Git 2.x
- Snowflake Account (for Module 5)

### Current Status

- [x] Module 1: GIT - Repository Setup ✅
- [ ] Module 2: Advanced Java - Domain Models
- [ ] Module 3: Spring Boot - REST APIs
- [ ] Module 4: Angular - Frontend
- [ ] Module 5: Snowflake - Analytics

## 📋 Core Features

- ✅ Fund Transfer between accounts
- ✅ Account Validation & Balance Check
- ✅ Transaction Logging & History
- ✅ Idempotency (Duplicate Prevention)
- ✅ User Authentication
- ✅ Business Analytics

## 🔒 Security

- Basic Auth / JWT Authentication
- Input Validation
- Transaction Management (ACID)
- Optimistic Locking

## 📊 Business Rules

1. Accounts must be different
2. Both accounts must exist and be ACTIVE
3. Amount must be greater than 0
4. Sufficient balance required
5. Idempotency key prevents duplicates





