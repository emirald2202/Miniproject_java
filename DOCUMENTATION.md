# Civilian Complaint Portal — Code Documentation

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Package Structure](#2-package-structure)
3. [Data Flow & Startup](#3-data-flow--startup)
4. [Package Details](#4-package-details)
   - [enums](#enums)
   - [exceptions](#exceptions)
   - [profile](#profile)
   - [users](#users)
   - [complaints](#complaints)
   - [containers](#containers)
   - [priority](#priority)
   - [search](#search)
   - [store](#store)
   - [threads](#threads)
   - [gui](#gui)
5. [Complaint Lifecycle (State Machine)](#5-complaint-lifecycle-state-machine)
6. [Notification Pipeline](#6-notification-pipeline)
7. [Priority Scoring Formula](#7-priority-scoring-formula)
8. [Threading Model](#8-threading-model)
9. [OOP Concepts Map](#9-oop-concepts-map)

---

## 1. Project Overview

The Civilian Complaint Portal is a JavaFX desktop application where citizens file civic complaints (infrastructure, corruption, noise, etc.), officers investigate them, and admins oversee the process. It demonstrates 7 core OOP concepts through a working system rather than toy examples.

**Key design choices:**
- All runtime data lives in one `DataStore` singleton — no database, no file I/O
- Each complaint category has its own typed `ComplaintBox<T>` — generics enforced at compile time
- Three background threads run independently of the GUI
- Each login window gets its own `SessionTimeoutThread` — multi-window safe

---

## 2. Package Structure

```
Miniproject_java/
├── Main.java                  ← Entry point: seeds demo data, starts threads, launches GUI
│
├── enums/                     ← All enum types
│   ├── Role.java              ← CITIZEN, OFFICER, ADMIN
│   ├── Status.java            ← FILED, UNDER_REVIEW, ESCALATED, RESOLVED, REJECTED
│   ├── ComplaintCategory.java ← 7 complaint types
│   └── OfficerDepartment.java ← 6 departments with category mappings
│
├── exceptions/                ← 6 domain-specific checked/runtime exceptions
│
├── profile/
│   └── CitizenProfile.java    ← Encapsulated sensitive citizen data
│
├── users/
│   ├── BaseUser.java          ← Abstract parent (userId, username, password, role)
│   ├── Citizen.java           ← Files complaints, views own profile
│   ├── Officer.java           ← Resolves complaints, has a department
│   └── Admin.java             ← Assigns officers, views all citizen data
│
├── complaints/
│   ├── BaseComplaint.java     ← Abstract parent with state machine logic
│   └── [7 subclasses]        ← One per category, each with its own priority formula
│
├── containers/
│   └── ComplaintBox.java      ← Generic typed container with duplicate detection
│
├── priority/
│   └── PriorityCalculator.java ← Static scoring formula + XOR log obfuscation
│
├── search/
│   └── ComplaintSearch.java   ← Three overloaded search() methods
│
├── store/
│   └── DataStore.java         ← Singleton holding all users, boxes, notification queue
│
├── threads/
│   ├── EscalationThread.java  ← Scans every 10s, auto-escalates high-priority complaints
│   ├── NotificationThread.java← Polls queue every 1s, delivers messages to dashboards
│   └── SessionTimeoutThread.java ← Per-window idle timer, logs out after inactivity
│
└── gui/
    ├── MainApp.java           ← JavaFX Application subclass (launch target)
    ├── LoginScreen.java       ← Login + citizen self-registration + multi-window
    ├── CitizenDashboard.java  ← File complaints, view own complaint history
    ├── OfficerDashboard.java  ← Review & update complaints (no citizen identity exposed)
    └── AdminDashboard.java    ← Assign officers, view citizen profiles, manage users
```

---

## 3. Data Flow & Startup

**`Main.java` startup sequence:**

1. Gets the `DataStore` singleton
2. Creates demo users (2 citizens, 2 officers, 1 admin) and adds them to `store.citizens` / `store.officers` / `store.admins`
3. Creates demo complaints using the correct subclass (e.g. `new InfrastructureComplaint(...)`) and calls `PriorityCalculator.calculateScore()` to set `priorityScore`
4. Adds each complaint to its matching typed box (e.g. `store.infraBox.addComplaint(...)`)
5. Starts `NotificationThread` — stores reference in `store.notificationThread`
6. Starts `EscalationThread` as a daemon thread
7. Calls `Application.launch(MainApp.class)` — hands off to JavaFX

**After login:**

- `LoginScreen` creates a `SessionTimeoutThread` for that window and passes it to the dashboard constructor
- The dashboard registers a notification callback with `store.notificationThread.registerCallback(userId, callback)`
- On logout, the callback is deregistered and the session thread is stopped

---

## 4. Package Details

### enums

Simple enumerations used throughout the system.

| Enum | Values | Used in |
|------|--------|---------|
| `Role` | `CITIZEN`, `OFFICER`, `ADMIN` | `BaseUser`, `LoginScreen` routing |
| `Status` | `FILED`, `UNDER_REVIEW`, `ESCALATED`, `RESOLVED`, `REJECTED` | `BaseComplaint`, state machine |
| `ComplaintCategory` | `INFRASTRUCTURE`, `CORRUPTION`, `NOISE`, `TRAFFIC`, `SANITATION`, `WATER_SUPPLY`, `ELECTRICITY` | Filing form, search, box routing |
| `OfficerDepartment` | `ACB`, `MSEB`, `PWD`, `LOCAL_POLICE`, `MUNICIPAL_CORPORATION`, `TRAFFIC_POLICE` | `Officer`, admin assignment UI |

**Department → Category mapping** (enforced in Admin GUI assignment logic):

| Department | Handles |
|------------|---------|
| ACB | Corruption |
| MSEB | Electricity |
| PWD | Infrastructure, Water Supply |
| LOCAL_POLICE | Noise |
| MUNICIPAL_CORPORATION | Sanitation |
| TRAFFIC_POLICE | Traffic |

---

### exceptions

Six custom exception classes, all extending `Exception` (checked) or `RuntimeException`:

| Exception | Thrown when |
|-----------|-------------|
| `DuplicateComplaintException` | Same citizen files same-titled complaint within 24 hours |
| `ComplaintNotFoundException` | `getComplaintById()` finds no match |
| `ComplaintExpiredException` | Modifying a `RESOLVED`/`REJECTED` complaint, or removing one archived >30 days |
| `InvalidStatusTransitionException` | Illegal state machine transition (e.g. `FILED → RESOLVED`) |
| `OfficerNotAssignedException` | Officer tries to modify a complaint not assigned to them |
| `UnauthorizedAccessException` | Non-admin tries to read `CitizenProfile` sensitive data |

---

### profile

**`CitizenProfile`** — demonstrates encapsulation.

All four fields (`aadhaarNumber`, `phoneNumber`, `homeAddress`, `fullName`) are `private` with no public getters. The only way to read them is through:

```java
String data = profile.getVerifiedData(admin);  // throws UnauthorizedAccessException if admin is null
```

This forces all callers to pass an `Admin` reference, preventing officers or citizens from accessing sensitive data.

---

### users

**`BaseUser`** (abstract) — defines the common interface:
- Fields: `userId`, `username`, `password`, `role`
- `login(username, password)` — credential check
- `performAction()` — abstract, each subclass prints a role-specific message

**`Citizen`** extends `BaseUser`:
- Holds a `CitizenProfile` (private field)
- `fileComplaint(T complaint, ComplaintBox<T> box)` — generic method, adds to the box
- `viewProfile(Admin admin)` — delegates to `CitizenProfile.getVerifiedData()`, returns null on denial

**`Officer`** extends `BaseUser`:
- Extra field: `OfficerDepartment department`
- Extra field: `assignedComplaints` counter
- `resolveComplaint(complaint)` — validates assignment and calls `complaint.updateStatus(RESOLVED)`

**`Admin`** extends `BaseUser`:
- Extra field: `adminLevel`
- `performAction()` override — prints admin-specific message

---

### complaints

**`BaseComplaint`** (abstract) — state machine lives here:

```
Fields: complaintId, title, description, status, filedByUserId,
        areaCode, urgencyLevel, filedDate, assignedToOfficerId, priorityScore
```

Key methods:
- `calculatePriorityScore()` — abstract; each subclass implements with a type-specific multiplier
- `updateStatus(newStatus)` — enforces the state machine; throws `InvalidStatusTransitionException` or `ComplaintExpiredException`
- `autoEscalate()` — bypasses the state machine; called only by `EscalationThread`
- `assignOfficer(officerId, requestingOfficerId)` — allows reassignment only by the currently assigned officer

**Valid transitions** (anything else throws `InvalidStatusTransitionException`):

```
FILED        → UNDER_REVIEW, REJECTED
UNDER_REVIEW → RESOLVED, ESCALATED, REJECTED
ESCALATED    → UNDER_REVIEW, RESOLVED
RESOLVED     → (terminal — throws ComplaintExpiredException)
REJECTED     → (terminal — throws ComplaintExpiredException)
```

**7 concrete subclasses** — each overrides `calculatePriorityScore()` with a different urgency multiplier:

| Subclass | Multiplier | Max score from subclass method |
|----------|-----------|-------------------------------|
| `NoiseComplaint` | urgency × 1 | 5 |
| `TrafficComplaint` | urgency × 1 | 5 |
| `InfrastructureComplaint` | urgency × 2 | 10 |
| `SanitationComplaint` | urgency × 2 | 10 |
| `CorruptionComplaint` | urgency × 3 | 15 |
| `WaterSupplyComplaint` | urgency × 3 | 15 |
| `ElectricityComplaint` | urgency × 3 | 15 |

> Note: The GUI uses `PriorityCalculator.calculateScore()` (range 5–33), not the subclass method, for the stored `priorityScore`. The subclass method exists as a polymorphism demo.

---

### containers

**`ComplaintBox<T extends BaseComplaint>`** — generic type-safe container.

The type parameter is bound at declaration:
```java
ComplaintBox<InfrastructureComplaint> infraBox = new ComplaintBox<>();
```

This means `infraBox.addComplaint(noiseComplaint)` is a **compile-time error** — wrong type rejected before runtime.

Key behaviour:
- `addComplaint(T)` — checks for duplicate (same citizen + same title + within 24 hours); throws `DuplicateComplaintException`
- `getComplaintById(int)` — linear scan; throws `ComplaintNotFoundException`
- `remove(int)` — throws `ComplaintExpiredException` if complaint is RESOLVED and >30 days old
- `getByPriority()` — returns a sorted copy (highest `priorityScore` first)

---

### priority

**`PriorityCalculator`** — all static utility methods.

**Scoring formula:**
```java
score = (complaintType << 2) + urgencyLevel
// complaintType: 1 (Infrastructure) to 7 (Electricity)
// urgencyLevel:  1 to 5
// Range: (1<<2)+1 = 5  to  (7<<2)+5 = 33
```

The left-shift `<< 2` multiplies complaint type by 4, giving it more weight than urgency (range 1–5).

**Auto-status thresholds:**
```
score > 20  → ESCALATED    (high-urgency Electricity/Corruption/Water types)
score > 12  → UNDER_REVIEW (medium priority)
score ≤ 12  → FILED        (low priority)
```
> The GUI always sets new complaints to `FILED` regardless; `autoAssignStatus()` is used in backend tests.

**XOR obfuscation:**
```java
String obfuscateLog(String message)  // XORs each char with 0b10101010
String decodeLog(String encoded)     // same operation — XOR is its own inverse
```

---

### search

**`ComplaintSearch`** — method overloading demo. Three `search()` methods resolved at **compile time** by argument type:

```java
search(5)                            // → search(int id)
search("ram")                        // → search(String username)
search(ComplaintCategory.CORRUPTION) // → search(ComplaintCategory category)
```

All three collect results from all 7 `ComplaintBox` instances in `DataStore` and return `List<BaseComplaint>`.

---

### store

**`DataStore`** — Singleton.

```java
DataStore store = DataStore.getInstance(); // always returns the same instance
```

Holds everything in memory:
- `List<Citizen> citizens`, `List<Officer> officers`, `List<Admin> admins`
- 7 typed `ComplaintBox` instances (one per category)
- `ConcurrentLinkedQueue<String> notificationQueue` — thread-safe; producers push, `NotificationThread` pops
- `HashMap<Integer, List<String>> userNotifications` — keyed by `userId`; stores delivered messages
- `NotificationThread notificationThread` — shared reference so dashboards can register callbacks

`ConcurrentLinkedQueue` is used (not `LinkedList`) because both the GUI thread and background threads push notifications concurrently without locking.

---

### threads

#### EscalationThread

Runs every **10 seconds**. Collects all complaints from all 7 boxes, and for each complaint that is `FILED` or `UNDER_REVIEW` with `priorityScore > 20`:
1. Calls `complaint.autoEscalate()` (direct field write, bypasses state machine)
2. Pushes a `"USERID:X|MSG:..."` string to `store.notificationQueue`

Uses `complaint.priorityScore` (set by `PriorityCalculator` when filed), not the subclass's `calculatePriorityScore()` method.

#### NotificationThread

Runs every **1 second**. Calls `store.notificationQueue.poll()` (non-blocking):
- If null: nothing to do, sleep and repeat
- If a message: parse `"USERID:X|MSG:text"` format, store `text` in `store.userNotifications.get(X)`, then fire the registered callback for user X if one exists

Dashboards register callbacks like:
```java
store.notificationThread.registerCallback(userId,
    () -> Platform.runLater(() -> { updateBellCount(); refreshData(); }));
```
The `Platform.runLater()` wrapper ensures GUI updates happen on the JavaFX Application Thread, not this background thread.

#### SessionTimeoutThread

One instance **per login window**. Checks every **1 second** how long since `lastActivityTime`. If idle ≥ `timeoutSeconds` (default: 300s), fires `onTimeoutCallback` (which calls `Platform.runLater(() -> goToLoginScreen())`) and stops.

Every button click, slider move, or key press in a dashboard calls `sessionThread.resetTimer()` which updates `lastActivityTime`. The `volatile` keyword ensures the write from the JavaFX thread is immediately visible to this background thread.

---

### gui

#### MainApp

Minimal `Application` subclass — just creates the primary `Stage` and sets the initial `LoginScreen` scene. `Application.launch()` can only be called once per JVM; additional windows are opened as plain `new Stage()` instances.

#### LoginScreen

- Iterates `store.citizens + store.officers + store.admins` to find a credential match
- Creates a `SessionTimeoutThread(300, timeoutCallback)` for the window, starts it, then calls `openDashboard(user, sessionThread)`
- **New Window button**: creates a new `Stage` and puts a fresh `LoginScreen` on it — each window has its own independent session thread
- **Register button**: shows a modal form to create a new `Citizen`; generates the next userId by scanning existing user lists

#### CitizenDashboard

- Left panel: `ListView` showing `[STATUS] title` for complaints filed by this citizen, color-coded by status. Clicking a row opens a detail popup.
- Center panel: filing form with category picker, area code, urgency slider (1–5), and live priority score preview
- On submit: creates the correct complaint subclass, calls `PriorityCalculator.calculateScore()`, always sets `Status.FILED`, adds to the matching box, notifies all admins
- Notification callback refreshes both the bell count and the complaint list using `getItems().setAll()` (avoids a JavaFX 17 IndexOutOfBoundsException from replacing the list while a selection is active)

#### OfficerDashboard

- Shows all complaints in a `TableView` (ID, title, status, priority, category columns)
- **Citizen identity is never shown** — citizen column is intentionally omitted for privacy
- Officer can update complaint status via dropdown; valid transitions are enforced by `BaseComplaint.updateStatus()`
- On status change: notifies the filing citizen, all admins, and the assigned officer (if different)

#### AdminDashboard

- Tab 1–3: one tab per department group showing complaints for relevant categories
- Assign officer: dropdown filtered to matching department officers; on assign, notifies citizen + officer
- View citizen: calls `citizen.viewProfile(admin)` — delegates to `CitizenProfile.getVerifiedData(admin)`, returns the encapsulated data
- Tab 4 — Manage Users: forms to add new Officer, Citizen, or Admin at runtime; new users appear immediately in `DataStore` and in the officer assignment dropdown

---

## 5. Complaint Lifecycle (State Machine)

```
                   [Citizen files complaint]
                           |
                      Status: FILED
                      priorityScore set
                           |
           ┌───────────────┴────────────────┐
           │ EscalationThread (every 10s)   │ Admin assigns officer
           │ score > 20 → autoEscalate()    │ → Status: UNDER_REVIEW
           ↓                                ↓
      Status: ESCALATED            Status: UNDER_REVIEW
           |                                |
     Higher authority              Officer decision:
     decision:                       ├── Resolve → RESOLVED (terminal)
     ├── Resolve → RESOLVED          ├── Escalate → ESCALATED
     └── De-escalate → UNDER_REVIEW  └── Reject  → REJECTED (terminal)
```

Terminal states (`RESOLVED`, `REJECTED`) throw `ComplaintExpiredException` on any further `updateStatus()` call. Resolved complaints older than 30 days also throw `ComplaintExpiredException` on `remove()`.

---

## 6. Notification Pipeline

```
[Event happens in GUI or EscalationThread]
            |
            | store.notificationQueue.offer("USERID:X|MSG:text")
            ↓
   ConcurrentLinkedQueue  (thread-safe, lock-free)
            |
            | NotificationThread polls every 1s
            ↓
   store.userNotifications.get(X).add(text)
            |
            | fires activeCallbacks.get(X).run()
            ↓
   Platform.runLater(() → updateBellCount() + refreshData())
            |
            ↓
   Bell counter updates; complaint table/list refreshes
```

**Who gets notified for each event:**

| Event | Recipients |
|-------|-----------|
| New complaint filed | All admins |
| Complaint assigned to officer | Assigned officer + filing citizen |
| Status changed (any) | Filing citizen + all admins + assigned officer (if any) |
| Auto-escalated by EscalationThread | Filing citizen |

---

## 7. Priority Scoring Formula

```
priorityScore = (complaintType << 2) + urgencyLevel
```

| Complaint Type | typeInt | urgency=1 | urgency=3 | urgency=5 |
|---------------|---------|-----------|-----------|-----------|
| Infrastructure | 1 | 5 | 7 | 9 |
| Corruption | 2 | 9 | 11 | 13 |
| Noise | 3 | 13 | 15 | 17 |
| Traffic | 4 | 17 | 19 | 21 |
| Sanitation | 5 | 21 | 23 | 25 |
| Water Supply | 6 | 25 | 27 | 29 |
| Electricity | 7 | 29 | 31 | 33 |

Scores above **20** are auto-escalated by `EscalationThread`.

---

## 8. Threading Model

```
JVM
├── JavaFX Application Thread  (GUI rendering, event handlers)
├── NotificationThread         (daemon) — polls queue every 1s
├── EscalationThread           (daemon) — scans complaints every 10s
└── SessionTimeoutThread × N   (daemon) — one per open login window
```

- `NotificationThread` and `EscalationThread` are started once in `Main.java` and run for the lifetime of the app.
- `SessionTimeoutThread` is created fresh on each login and stopped on logout or timeout.
- Background threads never touch JavaFX controls directly — they always wrap UI updates in `Platform.runLater()`.
- `volatile boolean running` in each thread ensures the stop flag is visible across threads without synchronization blocks.
- `ConcurrentLinkedQueue` is used for the notification queue — multiple threads push without needing `synchronized`.

---

## 9. OOP Concepts Map

| # | Concept | Primary file(s) | How it's used |
|---|---------|-----------------|---------------|
| 1 | Operators & Control Flow | `priority/PriorityCalculator.java` | Bitwise left-shift `<<` in priority formula; XOR `^` for log obfuscation |
| 2 | Encapsulation | `profile/CitizenProfile.java` | All 4 sensitive fields private, no public getters, admin-only access via `getVerifiedData()` |
| 3 | Method Overloading | `search/ComplaintSearch.java` | Three `search()` methods resolved at compile time by argument type |
| 4 | Inheritance & Polymorphism | `users/BaseUser.java`, `complaints/BaseComplaint.java` | Two separate inheritance trees; `performAction()` and `calculatePriorityScore()` overridden in every subclass |
| 5 | Generics | `containers/ComplaintBox.java` | `ComplaintBox<T extends BaseComplaint>` enforces type safety per complaint category at compile time |
| 6 | Custom Exceptions | `exceptions/` | 6 domain exceptions thrown and caught throughout the state machine, duplicate checks, and access control |
| 7 | Multithreading | `threads/` | 3 background threads with `volatile` flags, `ConcurrentLinkedQueue`, and `Platform.runLater()` for thread-safe GUI updates |
