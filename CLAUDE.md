# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

No build tool (Maven/Gradle) — compile directly with `javac`:

```bash
# Compile all packages in dependency order
javac -cp . enums/*.java exceptions/*.java profile/*.java
javac -cp . users/*.java complaints/*.java containers/*.java store/*.java
```

Or all at once (requires zsh/bash glob support):
```bash
javac -cp . **/*.java
```

There is no `main` method yet. The project is structured as a framework/library — to run it, create a driver class that uses the `DataStore` singleton, creates users, and exercises complaint workflows.

**Known compile issue**: `DataStore.java` uses `ConcurrentLinkedQueue` but is missing the import `java.util.concurrent.ConcurrentLinkedQueue`.

## Architecture

This is a **civic complaint management system** demonstrating core OOP concepts. The system is organized into 8 packages:

```
complaints/    → BaseComplaint (abstract) + 7 concrete complaint types
containers/    → ComplaintBox<T extends BaseComplaint> (generic container)
enums/         → Role, Status, ComplaintCategory, OfficerDepartment
exceptions/    → 6 custom domain exceptions
profile/       → CitizenProfile (encapsulation demo)
store/         → DataStore (Singleton holding all runtime state)
users/         → BaseUser (abstract) + Citizen, Officer, Admin
```

### Key Design Patterns

**Inheritance hierarchies:**
- `BaseUser` → `Citizen`, `Officer`, `Admin` — each overrides `performAction()`
- `BaseComplaint` → 7 subtypes — each implements `calculatePriorityScore()` with a type-specific urgency multiplier (1x–3x)

**Singleton:** `DataStore.getInstance()` holds all users, complaint boxes (one per complaint type), and a notification queue.

**Encapsulation:** `CitizenProfile` has no public getters — sensitive fields (Aadhaar, phone, address) only accessible via `getVerifiedData(Admin)`, which throws `UnauthorizedAccessException` for non-admin callers.

**Generics:** `ComplaintBox<T extends BaseComplaint>` is a type-safe container for a specific complaint subtype.

### Complaint Workflow (State Machine)

```
FILED → UNDER_REVIEW → RESOLVED
                     → REJECTED
                     → ESCALATED → UNDER_REVIEW
```
Terminal states `RESOLVED` and `REJECTED` cannot transition further. Invalid transitions throw `InvalidStatusTransitionException`.

### Department ↔ Complaint Category Mapping

| Department             | Handles                          |
|------------------------|----------------------------------|
| ACB                    | Corruption                       |
| MSEB                   | Electricity                      |
| PWD                    | Infrastructure, Water Supply     |
| LOCAL_POLICE           | Noise                            |
| MUNICIPAL_CORPORATION  | Sanitation                       |
| TRAFFIC_POLICE         | Traffic                          |

### Duplicate Prevention

Filing a complaint with the same citizen + same title within 24 hours throws `DuplicateComplaintException`.
