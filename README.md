# Civilian Complaint Portal

A Java OOP academic project demonstrating 7 core OOP concepts through a civic complaint management system.

## Prerequisites

- Java 17 (OpenJDK or Oracle)
- JavaFX 17 SDK — download **macOS / aarch64 / SDK** from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx)

## Setup

Extract the JavaFX SDK and copy the `lib/` folder into the project root:

```bash
cp -r ~/Downloads/javafx-sdk-17.x.x/lib ./lib
```

## Compile

Run these commands from the project root in order:

```bash
# 1. Compile base packages (no dependencies between them)
javac -cp . enums/*.java exceptions/*.java profile/*.java

# 2. Compile packages that depend on step 1
javac -cp . users/*.java complaints/*.java containers/*.java store/*.java priority/*.java

# 3. Compile threads and search
javac -cp . threads/*.java search/*.java

# 4. Compile GUI and entry point (requires JavaFX lib/)
javac --module-path lib --add-modules javafx.controls -cp . gui/*.java Main.java
```

Or as a single chained command:

```bash
javac -cp . enums/*.java exceptions/*.java profile/*.java && \
javac -cp . users/*.java complaints/*.java containers/*.java store/*.java priority/*.java && \
javac -cp . threads/*.java search/*.java && \
javac --module-path lib --add-modules javafx.controls -cp . gui/*.java Main.java
```

## Run

```bash
java --module-path lib --add-modules javafx.controls -cp . Main
```

At the startup menu:
- **[1]** Launch the JavaFX GUI
- **[2]** Run the backend test suite (terminal only)
- **[3]** Exit

## Demo Credentials

| Role    | Username  | Password |
|---------|-----------|----------|
| Citizen | `ram`     | `1234`   |
| Citizen | `priya`   | `1234`   |
| Officer | `officer1`| `pass`   |
| Officer | `officer2`| `pass`   |
| Admin   | `admin`   | `admin`  |

## OOP Concepts Demonstrated

| # | Concept | Where |
|---|---------|-------|
| 1 | Operators & Control Flow | `priority/PriorityCalculator.java` — bitwise priority scoring |
| 2 | Encapsulation | `profile/CitizenProfile.java` — no public getters; admin-only access |
| 3 | Method Overloading | `search/ComplaintSearch.java` — 3 `search()` overloads |
| 4 | Inheritance & Polymorphism | `users/BaseUser.java` → Citizen / Officer / Admin |
| 5 | Generics | `containers/ComplaintBox.java` — typed complaint containers |
| 6 | Custom Exceptions | `exceptions/` — 6 domain exceptions |
| 7 | Multithreading | `threads/` — EscalationThread, NotificationThread, SessionTimeoutThread |
