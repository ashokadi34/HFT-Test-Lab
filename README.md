# 🚀 HFT-Test-Lab

### End-to-End QA Automation Lab for a Simulated High-Frequency Trading Platform

HFT-Test-Lab is an educational and portfolio project that simulates a trading platform and demonstrates how a Senior QA / SDET engineer can validate a financial application across multiple layers.

The project focuses on **quality engineering and test automation**, rather than building a production-grade HFT engine.

It covers:

- REST API testing
- Database validation
- Order lifecycle validation
- Order matching and trade execution testing
- Price-time priority testing
- UI automation with Playwright
- End-to-end testing
- Concurrent and performance testing
- PostgreSQL integration
- Docker-based infrastructure
- GitHub Actions CI/CD

---

# 🎯 Project Objective

The objective of HFT-Test-Lab is to demonstrate an enterprise-style QA automation approach for a trading application.

The project validates a complete order lifecycle:

```text
Create Order
     │
     ▼
REST API
     │
     ▼
Request Validation
     │
     ▼
Order Processing
     │
     ▼
Order Matching Engine
     │
     ├───────────────┐
     ▼               ▼
Trade Execution   Order Status
     │               │
     └───────┬───────┘
             ▼
       PostgreSQL
             │
             ▼
      Trading Dashboard
             │
             ▼
       Playwright E2E
```

The project is intended to demonstrate testing practices applicable to:

- Financial applications
- Trading systems
- Capital markets
- Investment banking
- FinTech platforms
- Payment systems
- Enterprise web applications

---

# 🏗️ System Architecture

The current implementation is centered around a Spring Boot trading application.

```text
                    ┌──────────────────────────┐
                    │      Trading Dashboard   │
                    │       HTML / JS UI       │
                    └────────────┬─────────────┘
                                 │
                                 │ Playwright
                                 ▼
┌────────────────┐       ┌──────────────────────┐
│  REST Assured  │──────►│   Spring Boot API    │
│   API Tests    │       │   Order Controller   │
└────────────────┘       └──────────┬───────────┘
                                    │
                                    ▼
                           ┌─────────────────┐
                           │  Order Service  │
                           └────────┬────────┘
                                    │
                                    ▼
                           ┌─────────────────┐
                           │ Order Processor │
                           └────────┬────────┘
                                    │
                                    ▼
                           ┌─────────────────┐
                           │ Matching Engine │
                           └────────┬────────┘
                                    │
                           ┌────────┴────────┐
                           ▼                 ▼
                    ┌──────────────┐  ┌──────────────┐
                    │ Trade        │  │ Order Status │
                    │ Execution    │  │ History      │
                    └──────┬───────┘  └──────┬───────┘
                           │                 │
                           └────────┬────────┘
                                    ▼
                           ┌─────────────────┐
                           │   PostgreSQL    │
                           └─────────────────┘
```

---

# ⚙️ Technology Stack

| Category | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Build Tool | Maven |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| API Automation | REST Assured |
| UI Automation | Playwright |
| Test Framework | JUnit 5 |
| Performance Testing | Java-based concurrent/API tests |
| Containerization | Docker / Docker Compose |
| CI/CD | GitHub Actions |
| Version Control | Git |
| Database Validation | JDBC / SQL |

---

# 📁 Project Structure

```text
HFT-Test-Lab
│
├── .github
│   └── workflows
│       └── CI workflow
│
├── trade-system
│   │
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com.ashok.hft
│   │   │   │
│   │   │   └── resources
│   │   │       └── static
│   │   │           └── Trading Dashboard
│   │   │
│   │   └── test
│   │       ├── java
│   │       │   └── com.ashok.hft
│   │       │       ├── api
│   │       │       ├── db
│   │       │       ├── e2e
│   │       │       ├── performance
│   │       │       └── ui
│   │       │
│   │       └── resources
│   │           └── test data
│   │
│   ├── pom.xml
│   └── docker-compose.yml
│
├── .gitignore
└── README.md
```

---

# 🧪 Testing Strategy

The project follows a layered QA strategy.

```text
                 ┌────────────────────┐
                 │    E2E Testing     │
                 └─────────┬──────────┘
                           │
                 ┌─────────▼──────────┐
                 │    UI Testing      │
                 └─────────┬──────────┘
                           │
                 ┌─────────▼──────────┐
                 │ API + DB Testing   │
                 └─────────┬──────────┘
                           │
                 ┌─────────▼──────────┐
                 │  API Testing       │
                 └─────────┬──────────┘
                           │
                 ┌─────────▼──────────┐
                 │ Matching / Domain  │
                 │ Logic Validation   │
                 └─────────┬──────────┘
                           │
                 ┌─────────▼──────────┐
                 │ Performance /      │
                 │ Concurrency Testing │
                 └────────────────────┘
```

---

# 🔌 API Testing

REST Assured is used for API automation.

The API test suite covers:

- Order creation
- Request validation
- Response validation
- HTTP status validation
- Invalid symbol handling
- Invalid price handling
- Invalid quantity handling
- Invalid side handling
- Case-insensitive symbols
- Order rejection
- Database persistence
- Order status validation
- Order status history

Example order flow:

```text
POST /api/orders
       │
       ▼
Request Validation
       │
       ▼
Order Processing
       │
       ▼
Matching / Exchange Simulation
       │
       ▼
Response Validation
       │
       ▼
Database Validation
```

---

# 🗄️ Database Testing

PostgreSQL is used as the persistence layer.

Database validation includes:

- Order persistence
- Order status
- Order status history
- Trade persistence
- Order quantities
- Buy/Sell relationships
- Executed trade quantities
- Order matching results

The automation layer uses JDBC-based database utilities for direct SQL validation.

---

# 💱 Order Matching Engine

The project implements a simplified exchange/matching simulation for educational testing purposes.

The matching engine validates:

### Price Matching

For a BUY order:

```text
BUY price >= SELL price
```

For a SELL order:

```text
SELL price <= BUY price
```

### Price-Time Priority

Active orders are considered using price and creation-time ordering.

The project includes dedicated automation for:

- BUY/SELL matching
- Partial fills
- Multiple matching orders
- Trade creation
- Price-time priority
- Order status transitions
- Trade persistence

---

# 🖥️ Trading Dashboard

The application includes a browser-based trading dashboard.

The dashboard provides:

- Order submission
- BUY / SELL selection
- Order listing
- Order status
- Order Book
- Symbol filtering
- Side filtering
- Minimum price filtering
- Maximum price filtering
- Order Book pagination
- Refresh functionality
- Trading/order information display

Example workflow:

```text
Enter Symbol
     ↓
Enter Price
     ↓
Enter Quantity
     ↓
Select BUY / SELL
     ↓
Submit Order
     ↓
Verify Order
     ↓
Verify Order Book
```

---

# 🎭 UI Automation

Playwright is used for browser automation.

The UI automation follows the Page Object Model.

Example:

```text
OrderDashboardUiTest
        │
        ▼
OrderDashboardPage
        │
        ▼
Trading Dashboard
```

The UI suite validates:

- Order creation
- Order table
- Order Book
- Order Book filtering
- Side filtering
- Price filtering
- Pagination
- Dashboard refresh
- Dynamic order verification

### Latest UI Regression

```text
OrderDashboardUiTest

Tests Run : 14
Failures  : 0
Errors    : 0

Result    : PASS
```

---

# 🔄 End-to-End Testing

The project contains an E2E test covering the critical order journey across application layers.

### E2E-001

```text
REST API
   │
   ▼
Create Order
   │
   ▼
API Response
   │
   ▼
PostgreSQL
   │
   ▼
Trading Dashboard
   │
   ▼
Order Book
```

E2E-001 validates:

- API response
- Generated order ID
- Symbol
- Side
- Database persistence
- Database status
- Dashboard order visibility
- Order Book visibility

### Latest E2E Result

```text
E2E-001

API            : PASS
Database       : PASS
Dashboard      : PASS
Order Book     : PASS
Overall        : PASS
```

---

# ⚡ Performance & Concurrency Testing

The project includes Java-based API performance and concurrency tests.

The performance suite measures:

- Response time
- Minimum latency
- Maximum latency
- Average latency
- P50
- P95
- P99
- Throughput
- Successful requests
- Failed requests
- Concurrent order submission

## PERF-005 Performance Gate

Latest recorded result:

```text
Total Requests       : 50
Successful Requests  : 50
Failed Requests      : 0

Total Duration       : 5123 ms
Average Response     : 101.80 ms
Minimum Response     : 83 ms
Maximum Response     : 266 ms

P50 Response         : 96 ms
P95 Response         : 145 ms
P99 Response         : 266 ms

Throughput           : 9.76 req/sec

P95 Threshold        : 300 ms
Performance Gate     : PASS
```

> These measurements are local test-environment results and should not be interpreted as production HFT latency benchmarks.

---

# 🔁 CI/CD

GitHub Actions is used to automate project validation.

The CI pipeline includes:

- Maven build
- Spring Boot test execution
- API tests
- UI tests
- Performance tests
- PostgreSQL service
- Test result reporting

## CI Results

### CI-001

```text
Maven / API / UI / Performance pipeline

Result: PASS
```

### CI-002

```text
Price-Time Priority Test Data Isolation

Result: PASS
```

CI-002 addressed test-data contamination caused by persistent active orders and isolated the price-time priority test data using dedicated test symbols/prices.

---

# 🧩 Test Suite

Current major test areas include:

```text
src/test/java/com/ashok/hft

├── api
│   ├── OrderApiTest
│   ├── OrderApiDbTest
│   ├── OrderValidationApiTest
│   ├── OrderRejectedApiTest
│   ├── OrderMatchingApiDbTest
│   ├── OrderMultiMatchApiDbTest
│   ├── OrderPriceTimePriorityApiDbTest
│   ├── OrderCaseInsensitiveSymbolApiDbTest
│   └── OrderStatusHistoryApiDbTest
│
├── db
│   └── DatabaseUtils
│
├── e2e
│   └── OrderEndToEndTest
│
├── performance
│   ├── ConcurrentOrderSubmissionTest
│   ├── OrderApiLatencyTest
│   ├── OrderApiPerformanceTest
│   ├── OrderApiPerformanceThresholdTest
│   └── OrderApiThroughputTest
│
└── ui
    ├── OrderDashboardUiTest
    └── pages
        └── OrderDashboardPage
```

---

# 📊 Regression Status

The latest local full regression executed:

```text
Tests Executed : 32
Passed         : 29
Failed         : 3
Errors         : 0
```

The three failures were observed in matching-engine database tests:

```text
OrderMatchingApiDbTest
OrderMultiMatchApiDbTest
OrderPriceTimePriorityApiDbTest
```

These failures were associated with persistent/shared test data and active orders in the PostgreSQL environment.

The project already contains dedicated test-data isolation for the critical CI-002 price-time-priority scenario.

The latest UI regression independently completed:

```text
14 / 14 PASS
```

The dedicated E2E-001 flow also completed successfully:

```text
API            : PASS
Database       : PASS
Dashboard      : PASS
Order Book     : PASS
```

---

# 🐳 Docker / PostgreSQL

PostgreSQL is containerized using Docker Compose.

The environment is used for:

- Application persistence
- API + database validation
- Matching-engine testing
- Trade persistence
- E2E validation
- CI execution

Typical local environment:

```text
Spring Boot Application
        │
        │ JDBC
        ▼
PostgreSQL Container
```

---

# ▶️ How to Run

## 1. Start PostgreSQL

From the project directory:

```bash
docker compose up -d
```

Verify the container:

```bash
docker ps
```

---

## 2. Start the application

From:

```text
HFT-Test-Lab/trade-system
```

run:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Duser.timezone=Asia/Kolkata"
```

---

## 3. Run all tests

```bash
mvn clean test
```

---

## 4. Run API tests

```bash
mvn -Dtest=OrderApiTest test
```

---

## 5. Run UI tests

```bash
mvn -Dtest=OrderDashboardUiTest test
```

---

## 6. Run E2E test

```bash
mvn -Dtest=OrderEndToEndTest test
```

---

## 7. Run performance threshold test

```bash
mvn -Dtest=OrderApiPerformanceThresholdTest test
```

---

# 🧠 QA Engineering Practices Demonstrated

This project demonstrates practical QA/SDET concepts including:

- Test automation framework design
- Page Object Model
- REST API automation
- Database validation
- SQL/JDBC validation
- Positive and negative testing
- Boundary-value validation
- Data-driven testing
- Order lifecycle validation
- Business-rule validation
- Price-time priority testing
- Trade validation
- End-to-end testing
- UI synchronization
- Dynamic test data generation
- Concurrent request testing
- Performance thresholds
- Test isolation
- CI/CD automation
- Docker-based test environments
- Regression testing

---

# ⚠️ Project Scope & Limitations

This is an **educational trading-system simulation**, not a production HFT engine.

It does not attempt to reproduce:

- Exchange-grade network latency
- Hardware acceleration
- FPGA-based execution
- Real market feeds
- FIX connectivity
- Production risk infrastructure
- Distributed exchange infrastructure
- Real-money trading
- Production-scale throughput

The performance numbers in this repository are intended to demonstrate **QA performance-testing methodology**, not production HFT performance.

The current test environment also uses a persistent PostgreSQL database. Some matching tests can be affected by previously created active orders when symbols are reused across test executions.

---

# 🤝 Contributing

This repository is currently an individual learning and portfolio project.

Suggestions and improvements are welcome.

---

# 🚀 Future Enhancements

Possible future improvements include:

- Kafka/event-driven order processing
- FIX protocol simulation
- Market-data replay
- Prometheus metrics
- Grafana dashboards
- Distributed performance testing
- Testcontainers-based isolated test environments
- Improved test-data lifecycle management
- Additional order types
- Advanced order-book scenarios

---

# 👨‍💻 Skills Demonstrated

```text
Java
Spring Boot
Maven
PostgreSQL
SQL
JDBC
REST Assured
Playwright
JUnit 5
Docker
Docker Compose
Git
GitHub Actions
API Testing
Database Testing
UI Automation
Performance Testing
Concurrency Testing
End-to-End Testing
Test Automation Framework Design
```

---

# 📌 Project Status

```text
Core Trading Application       ✅
PostgreSQL Integration         ✅
REST API Automation            ✅
Database Validation            ✅
Order Matching Tests           ✅
Price-Time Priority            ✅
UI Automation                  ✅
Order Book Automation          ✅
Performance Testing            ✅
CI/CD                          ✅
E2E Testing                    ✅
Final Documentation            🚧
```

---

# 📄 License

This project is intended for educational and portfolio purposes.

*A portfolio project demonstrating real-world Software Testing, Automation, API Testing, Database Testing, UI Testing, Performance Testing, and Infrastructure Testing for High Frequency Trading Systems.*

![Java](https://img.shields.io/badge/Java-22-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![Docker](https://img.shields.io/badge/Docker-Latest-2496ED)
![Playwright](https://img.shields.io/badge/Playwright-Java-green)
![REST Assured](https://img.shields.io/badge/REST%20Assured-API-red)
![JUnit5](https://img.shields.io/badge/JUnit-5-red)
![GitHub Actions](https://img.shields.io/badge/CI/CD-GitHub%20Actions-black)

## Thank you!