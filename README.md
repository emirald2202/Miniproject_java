# Civic Complaint Management System

> A Java-based complaint management system with a **JavaFX GUI**, demonstrating core **Object-Oriented Programming** concepts — built as a mini project for OOPD coursework.

---

## Table of Contents

- [Quick Start](#quick-start)
- [Overview](#overview)
- [OOP Concepts Demonstrated](#oop-concepts-demonstrated)
- [Project Structure](#project-structure)
- [Class Hierarchy](#class-hierarchy)
- [Custom Exceptions](#custom-exceptions)
- [Status Workflow](#status-workflow)
- [Demo Credentials](#demo-credentials)
- [Manual Build & Run](#manual-build--run)
- [Tech Stack](#tech-stack)

---

## Quick Start

### Prerequisites

- **Java JDK 17+** installed
  - Linux: `sudo apt install openjdk-17-jdk`
  - macOS: `brew install openjdk@17`
  - Windows: Download from [Adoptium](https://adoptium.net/)

### One-command launch

Each OS has a pre-configured run script. Just clone and run:

#### Linux
```bash
chmod +x run_linux.sh
./run_linux.sh            # Launch GUI
./run_linux.sh --test     # Run backend tests (no GUI)
```
> Uses `lib_linux/` — bundled JavaFX **Linux x64** native libraries (`.so` files).

#### macOS
```bash
chmod +x run_mac.sh
./run_mac.sh              # Launch GUI
./run_mac.sh --test       # Run backend tests (no GUI)
```
> Uses `lib_mac/` — bundled JavaFX **macOS** native libraries (`.dylib` files).

#### Windows
```cmd
run_windows.bat            REM Launch GUI
run_windows.bat --test     REM Run backend tests (no GUI)
```
> Uses `lib_win/` — bundled JavaFX **Windows** native libraries (`.dll` files).

---

## Overview

The system models a **civic complaint management workflow** where:

- **Citizens** file complaints across 7 categories (Water Supply, Electricity, Infrastructure, etc.)
- **Officers** from specific departments investigate and resolve assigned complaints
- **Admins** manage the system, assign officers, and access sensitive citizen data
- **Custom exceptions** enforce business rules at every critical operation
- **Background threads** auto-escalate high-priority complaints and deliver live notifications

All data is held in-memory via a **Singleton `DataStore`**, and complaints are stored in **generic `ComplaintBox<T>`** containers — one per category.

---

## OOP Concepts Demonstrated

| # | Concept | Where Used | Assignment |
|---|---|---|---|
| 1 | **Control Flow & Operators** | `Main.java` (switch, if-else), `PriorityCalculator` (bitwise `<<`, `\|`, `^`) | 1 |
| 2 | **Encapsulation** | `CitizenProfile` — private fields, access only via `getVerifiedData(Admin)` | 2 |
| 3 | **Method Overloading** | `ComplaintSearch.search(int)`, `search(String)`, `search(ComplaintCategory)` | 3 |
| 4 | **Inheritance & Polymorphism** | `BaseUser` → 3 subclasses; `BaseComplaint` → 7 subclasses; `performAction()` overrides | 4 |
| 5 | **Generics** | `ComplaintBox<T extends BaseComplaint>` — type-safe complaint containers | 5 |
| 6 | **Exception Handling** | 6 custom exceptions with try-catch throughout the codebase | 6 |
| 7 | **Multithreading** | `EscalationThread`, `NotificationThread`, `SessionTimeoutThread` | 7 |
| — | **Enums** | `Status`, `Role`, `ComplaintCategory`, `OfficerDepartment` | — |
| — | **Singleton Pattern** | `DataStore.getInstance()` — single global data store | — |

---

## Project Structure

```
Miniproject_java/
├── Main.java                          # Entry point — loads data, launches GUI or tests
├── run_linux.sh                       # One-click run for Linux
├── run_mac.sh                         # One-click run for macOS
├── run_windows.bat                    # One-click run for Windows
├── lib_mac/                           # JavaFX SDK — macOS native (.dylib)
├── lib_win/                           # JavaFX SDK — Windows native (.dll)
├── lib_linux/                         # JavaFX SDK — Linux x64 native (.so)
│
├── gui/                               # JavaFX GUI screens
│   ├── MainApp.java                   # JavaFX Application entry point
│   ├── LoginScreen.java               # Login with role selection
│   ├── CitizenDashboard.java          # File complaints, view history, notifications
│   ├── OfficerDashboard.java          # View/search/update complaints (no citizen PII)
│   └── AdminDashboard.java            # Manage system, assign officers, XOR log demo
│
├── complaints/                        # Complaint hierarchy
│   ├── BaseComplaint.java             # Abstract parent (status workflow + officer assignment)
│   ├── WaterSupplyComplaint.java      # priority = urgency x 3
│   ├── ElectricityComplaint.java      # priority = urgency x 3
│   ├── CorruptionComplaint.java       # priority = urgency x 3
│   ├── SanitationComplaint.java       # priority = urgency x 2
│   ├── InfrastructureComplaint.java   # priority = urgency x 2
│   ├── TrafficComplaint.java          # priority = urgency x 1
│   └── NoiseComplaint.java            # priority = urgency x 1
│
├── users/                             # User hierarchy
│   ├── BaseUser.java                  # Abstract parent (login, performAction)
│   ├── Citizen.java                   # Files complaints, views profile
│   ├── Officer.java                   # Resolves complaints (with assignment check)
│   └── Admin.java                     # System management, data access
│
├── containers/                        # Generic data structures
│   └── ComplaintBox.java              # ComplaintBox<T> with add/get/remove/sort
│
├── exceptions/                        # Custom exception classes
│   ├── DuplicateComplaintException.java
│   ├── UnauthorizedAccessException.java
│   ├── ComplaintExpiredException.java
│   ├── InvalidStatusTransitionException.java
│   ├── OfficerNotAssignedException.java
│   └── ComplaintNotFoundException.java
│
├── enums/                             # System enumerations
│   ├── Status.java                    # FILED -> UNDER_REVIEW -> RESOLVED / ESCALATED / REJECTED
│   ├── Role.java                      # CITIZEN, OFFICER, ADMIN
│   ├── ComplaintCategory.java         # 7 complaint categories
│   └── OfficerDepartment.java         # ACB, MSEB, PWD, LOCAL_POLICE, etc.
│
├── priority/                          # Priority scoring
│   └── PriorityCalculator.java        # Bitwise scoring + XOR log obfuscation
│
├── search/                            # Search functionality
│   └── ComplaintSearch.java           # Overloaded search(int/String/Category)
│
├── threads/                           # Background threads
│   ├── EscalationThread.java          # Auto-escalates high-priority complaints
│   ├── NotificationThread.java        # Delivers queued notifications to dashboards
│   └── SessionTimeoutThread.java      # Logs out idle users
│
├── profile/                           # Secure data
│   └── CitizenProfile.java            # Encapsulated citizen PII (Aadhaar, phone, address)
│
└── store/                             # Data persistence
    └── DataStore.java                 # Singleton — holds all runtime data
```

---

## Class Hierarchy

```
BaseUser (abstract)
├── Citizen        → files complaints, views profile
├── Officer        → resolves complaints (assignment-checked)
└── Admin          → manages system, accesses citizen data

BaseComplaint (abstract)
├── WaterSupplyComplaint
├── ElectricityComplaint
├── CorruptionComplaint
├── SanitationComplaint
├── InfrastructureComplaint
├── TrafficComplaint
└── NoiseComplaint

Exception
├── DuplicateComplaintException
├── UnauthorizedAccessException
├── ComplaintExpiredException
├── InvalidStatusTransitionException
├── OfficerNotAssignedException
└── ComplaintNotFoundException
```

---

## Custom Exceptions

All 6 exceptions extend `Exception` and include both `(String message)` and `(String message, Throwable cause)` constructors.

| # | Exception | Thrown When | Protects Against |
|---|---|---|---|
| 1 | `DuplicateComplaintException` | Same citizen files same-titled complaint within 24 hrs | Data corruption |
| 2 | `UnauthorizedAccessException` | Non-admin tries to access citizen PII | Sensitive data leak |
| 3 | `ComplaintExpiredException` | Operation on a RESOLVED/REJECTED complaint | Lifecycle violation |
| 4 | `InvalidStatusTransitionException` | Invalid status transition (e.g., FILED → RESOLVED) | Inconsistent state |
| 5 | `OfficerNotAssignedException` | Unassigned officer tries to modify a complaint | Unauthorized modification |
| 6 | `ComplaintNotFoundException` | Complaint lookup by ID fails | Null pointer / undefined behavior |

---

## Status Workflow

Valid complaint status transitions enforced by `InvalidStatusTransitionException`:

```
                    +--------------+
                    |    FILED     |
                    +------+-------+
                           |
                 +---------v---------+
                 |   UNDER_REVIEW    |
                 +-+-------+-------+-+
                   |       |       |
          +--------+-+     |   +---+--------+
          | ESCALATED |     |   |  REJECTED  |
          +----+------+     |   +------------+
               |            |
               +------+-----+
                      |
                +-----v-----+
                |  RESOLVED  |
                +------------+
```

**Rules:**
- `FILED` → `UNDER_REVIEW` or `REJECTED`
- `UNDER_REVIEW` → `RESOLVED`, `ESCALATED`, or `REJECTED`
- `ESCALATED` → `UNDER_REVIEW` or `RESOLVED`
- `RESOLVED` / `REJECTED` → No further transitions (throws `ComplaintExpiredException`)

---

## Demo Credentials

The system comes pre-loaded with test data. Use these credentials on the login screen:

| Role | Username | Password |
|---|---|---|
| Citizen | `ram` | `1234` |
| Citizen | `priya` | `1234` |
| Officer | `officer1` | `pass` |
| Officer | `officer2` | `pass` |
| Admin | `admin` | `admin` |

---

## Manual Build & Run

If you prefer not to use the scripts:

### Compile
```bash
# Linux
javac --module-path lib_linux --add-modules javafx.controls -cp . \
    enums/*.java exceptions/*.java profile/*.java users/*.java \
    complaints/*.java containers/*.java store/*.java priority/*.java \
    search/*.java threads/*.java gui/*.java Main.java

# macOS
javac --module-path lib_mac --add-modules javafx.controls -cp . \
    enums/*.java exceptions/*.java profile/*.java users/*.java \
    complaints/*.java containers/*.java store/*.java priority/*.java \
    search/*.java threads/*.java gui/*.java Main.java

# Windows
javac --module-path lib_win --add-modules javafx.controls -cp . \
    enums/*.java exceptions/*.java profile/*.java users/*.java \
    complaints/*.java containers/*.java store/*.java priority/*.java \
    search/*.java threads/*.java gui/*.java Main.java
```

### Run GUI
```bash
# Linux
java --module-path lib_linux --add-modules javafx.controls -cp . Main

# macOS
java --module-path lib_mac --add-modules javafx.controls -cp . Main

# Windows
java --module-path lib_win --add-modules javafx.controls -cp . Main
```

### Run Backend Tests (no GUI)
```bash
# Linux
java --module-path lib_linux --add-modules javafx.controls -cp . Main --test

# macOS
java --module-path lib_mac --add-modules javafx.controls -cp . Main --test

# Windows
java --module-path lib_win --add-modules javafx.controls -cp . Main --test
```

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17+ |
| GUI | JavaFX 17 |
| Build | `javac` (no Maven/Gradle required) |
| Architecture | Layered package structure |
| Data Storage | In-memory (Singleton `DataStore`) |
| Testing | Backend test suite (`Main --test`) |

---

## Team

Built as a mini project for **OOP Design** coursework.

---

<p align="center">
  <i>Demonstrating clean OOP design with robust exception handling and a full JavaFX GUI.</i>
</p>
