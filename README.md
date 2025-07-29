# Burse

Burse is a simplified stock exchange simulation system that handles buy and sell offers from multiple traders in real time. It matches offers, executes trades, and maintains accurate records of offers and transactions, while supporting concurrency through Redis-based locking.

## Table of Contents

1. [Key Features](#key-features)
2. [Architecture](#architecture)
3. [Usage Scenarios](#usage-scenarios)
4. [Technologies Used](#technologies-used)
5. [Running Locally](#running-locally)
6. [Tests](#tests)
7. [Project Structure](#project-structure)
8. [Design Decisions](#design-decisions)

---

## Key Features

* Accepts buy and sell offers for different stocks from multiple traders.
* Prevents conflicting offers (both buy and sell from the same trader on the same stock).
* Matches offers automatically based on price and submission time.
* Executes trades atomically, ensuring consistency under concurrency.
* Archives completed and canceled offers in a separate table.
* Supports dynamic, switchable pricing strategies.
* Provides a documented REST API via Swagger.
* Integration tests cover critical and edge-case flows.
* The system is designed to ensure that no two matching offers remain in the system without being automatically processed into a trade.

---

## Architecture

The system follows a modular layered architecture with a clear separation of concerns:

* Each resource (Trader, Stock, Offer, Trade) has its own module: `controller`, `service`, and `repository`.
* Services are built as interfaces with implementations to allow flexibility.
* DTOs isolate internal entities from API exposure.
* `locks/` manages Redis-based distributed locking.
* `pricing/` contains pluggable strategy implementations.
* `config/` holds system-wide configuration (e.g., Redis, Swagger).

### Code Conventions

- Class and file names start with **uppercase letters** (PascalCase).
- Methods and variables use **camelCase**.
- URL paths use **kebab-case** (e.g., `/sell-offer`).
- Test method names use descriptive camelCase with underscores to separate context and expectation (e.g., `offerCancel_whenNoFunds`).

### Layering Principles

- No **service** directly accesses **repository** of another module.
- Crosses between **controller and services** were avoided, except for a single justified case.


---

## Usage Scenarios

### 1. Submitting Offers

Traders submit buy/sell offers for a specific stock. The system:

* Verifies no conflicting offer exists for the same trader and stock.
* Checks for matching offer based on price/time.
* Executes a trade immediately if a match is found.
* If no match, the offer is saved for future matching.

### 2. Trade Execution

* Both offers are locked to prevent concurrent usage.
* Trader funds and stock balances are locked and verified.
* Money and stocks are exchanged.
* Fully filled offers are archived.
* Partially filled offer is updated and re-matched (Because it might be skipped while it was locked).

### 3. Initialization

At startup:

* Predefined traders and stocks are loaded.
* BURSE (a fictional trader) creates default sell offers.

### 4. Strategy Switching

A strategy can be dynamically changed via an endpoint.

---

## Technologies Used

* **Java 21**, **Spring Boot**, **Spring Data JPA**, **PostgreSQL**
* **Redis** with **Redisson** for locking
* **Lombok**, **SLF4J**, **Logback**
* **OpenAPI/Swagger** for API docs
* **JUnit 5**, **Spring Boot Test**, **MockMvc** for testing
* **Maven**, **Git**, **GitKraken** for tooling

---

## Running Locally

### Requirements

* Java 21+, Maven 3.8+, Redis, PostgreSQL

### 1. Clone Repository

```bash
git clone https://github.com/AyalaHakarmi/burse-backend.git
cd burse-backend
```

### 2. PostgreSQL Configuration

In `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/burse
spring.datasource.username=postgres
spring.datasource.password=4167
```
Change if needed.

- The application uses the setting:
```properties
  spring.jpa.hibernate.ddl-auto=create-drop
```
- This means the database schema will be created and dropped on each run.
- Do not use this setting in production.


### 3. Redis Configuration

In `redisson.yaml`:

```yaml
singleServerConfig:
  address: "redis://127.0.0.1:6379"
```

Change if needed to match your Redis host.

- On application startup, all Redis keys are automatically deleted.
  This is done inside the `BurseInitializer`.
  Change it in production.

### 4. Run the Application

```bash
mvn spring-boot:run
```

Default port: `8081`

### 5. Swagger UI

Access at:
`http://localhost:8081/swagger-ui/index.html`

### 6. Logging

```properties
logging.level.root=ERROR
logging.level.com.burse.bursebackend=INFO
```
Adjust levels for debugging (`DEBUG`, `TRACE`).

---

## Tests

Run all tests:

```bash
mvn test
```

### Test Coverage

* Valid and invalid offer submissions
* Trade logic, matching, and partial fills
* Locking behavior under concurrency
* Error handling and edge cases
* Stock price strategy switching
* Stock price updates
* Archiving and DTO responses
* etc.
---

## Project Structure

```
com.burse.bursebackend
├── controllers         # REST API endpoints
├── services            # Business logic with interfaces + implementations . 
├── repositories        # JPA Repositories
├── entities            # Domain model (Trader, Stock, Offer, etc.)
├── dtos                # DTOs for API communication
├── exceptions          # Custom exceptions and handlers
├── locks               # Redis lock services
├── pricing             # Strategy for stock prices updates
├── types               # Enums 
├── config              # App-wide configuration 


└── resources
   ├── data
       └── BurseJson.json
   ├── application.properties
   ├── redisson.yaml
    
```

---

## Design Decisions

### 1. Archiving Offers

* Offers are never deleted.
* Archived offers are stored in a **separate table** (`archived_offer`) to avoid impacting performance when querying active offers.


### 2. Offer Inheritance

* `Offer` (mapped superclass)
* abstract entity `ActiveOffer`(extends `Offer`) → `BuyOffer` / `SellOffer`
* `ArchivedOffer` = flattened snapshot (extends `Offer`)

### 3. Redis Locking

* Prevents double-use of offers in trades
* Prevents conflicting submissions of offers
* Locks buyer money and seller stock's holding during trade

### 4. Matching Priorities

* Match by the best price, then earliest time
* Trade price = earlier offer’s price
* Quantity = minimum of the two
* Remaining quantity → offer updated and re-matched
* If a match between offers is found but the trader no longer has sufficient funds (for a buy) or stock (for a sell), the offer is **automatically canceled and archived**.

### 5. DTO Usage

* API uses DTOs only (with `DTO` suffix)
* Internal structure is never exposed
* Aggregated DTOs to **GET** endpoints are built via `BurseViewService`

### 6. Interpretation-Based Design Choices

Certain behaviors in the system were implemented based on interpretation of the task requirements:

- Separate endpoints were defined for:
    - Submitting buy offers vs. sell offers
    - Canceling buy offers vs. sell offers
- Trader names were returned to the frontend **without their IDs**.
- Stock names were not returned in certain responses, **just their IDs**, as they were not explicitly required.


## Thanks :)



