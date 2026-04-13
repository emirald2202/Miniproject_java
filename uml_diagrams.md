# UML Diagrams — Civilian Complaint Portal

> **Project:** Miniproject_java — OOP Assignment (JavaFX)
> **Diagrams covered:** Structural (Class, Object, Component, Package) + Behavioral (Use Case, Sequence ×3, Activity, State Machine)

---

## 📐 STRUCTURAL DIAGRAMS

---

### 1. Class Diagram

> Shows all classes, their attributes, methods, and relationships (inheritance, association, dependency, generics).

```mermaid
classDiagram
    %% ─── ENUMS ───────────────────────────────────────────────────────────────
    class Role {
        <<enumeration>>
        CITIZEN
        OFFICER
        ADMIN
    }

    class Status {
        <<enumeration>>
        FILED
        UNDER_REVIEW
        ESCALATED
        RESOLVED
        REJECTED
    }

    class OfficerDepartment {
        <<enumeration>>
        ACB
        MSEB
        PWD
        LOCAL_POLICE
        MUNICIPAL_CORPORATION
        TRAFFIC_POLICE
    }

    class ComplaintCategory {
        <<enumeration>>
        INFRASTRUCTURE
        CORRUPTION
        NOISE
        TRAFFIC
        SANITATION
        WATER_SUPPLY
        ELECTRICITY
    }

    %% ─── USER HIERARCHY ──────────────────────────────────────────────────────
    class BaseUser {
        <<abstract>>
        +int userId
        +String username
        +String password
        +Role role
        +login(String, String) boolean
        +performAction()* void
    }

    class Citizen {
        -CitizenProfile profile
        +performAction() void
        +fileComplaint(T, ComplaintBox~T~) void
        +viewProfile(Admin) String
        +getProfile() CitizenProfile
    }

    class Officer {
        +int assignedComplaints
        +OfficerDepartment department
        +performAction() void
        +resolveComplaint(BaseComplaint) void
    }

    class Admin {
        +performAction() void
    }

    BaseUser <|-- Citizen
    BaseUser <|-- Officer
    BaseUser <|-- Admin
    Citizen --> CitizenProfile : has-a
    Officer --> OfficerDepartment : uses
    BaseUser --> Role : uses

    %% ─── PROFILE ─────────────────────────────────────────────────────────────
    class CitizenProfile {
        -String aadhaarNumber
        -String phoneNumber
        -String homeAddress
        -String fullName
        +getVerifiedData(Admin) String
    }

    CitizenProfile ..> Admin : depends on

    %% ─── COMPLAINT HIERARCHY ─────────────────────────────────────────────────
    class BaseComplaint {
        <<abstract>>
        +int complaintId
        +String title
        +String description
        +Status status
        +int filedByUserId
        +int areaCode
        +int urgencyLevel
        +LocalDateTime filedDate
        +int assignedToOfficerId
        +int priorityScore
        +calculatePriorityScore()* int
        +updateStatus(Status) void
        +autoEscalate() void
        +assignOfficer(int, int) void
    }

    class InfrastructureComplaint {
        +calculatePriorityScore() int
    }
    class CorruptionComplaint {
        +calculatePriorityScore() int
    }
    class NoiseComplaint {
        +calculatePriorityScore() int
    }
    class TrafficComplaint {
        +calculatePriorityScore() int
    }
    class SanitationComplaint {
        +calculatePriorityScore() int
    }
    class WaterSupplyComplaint {
        +calculatePriorityScore() int
    }
    class ElectricityComplaint {
        +calculatePriorityScore() int
    }

    BaseComplaint <|-- InfrastructureComplaint
    BaseComplaint <|-- CorruptionComplaint
    BaseComplaint <|-- NoiseComplaint
    BaseComplaint <|-- TrafficComplaint
    BaseComplaint <|-- SanitationComplaint
    BaseComplaint <|-- WaterSupplyComplaint
    BaseComplaint <|-- ElectricityComplaint
    BaseComplaint --> Status : uses

    %% ─── GENERIC CONTAINER ───────────────────────────────────────────────────
    class ComplaintBox~T~ {
        -List~T~ complaints
        +addComplaint(T) void
        +getComplaintById(int) T
        +getAllComplaints() List~T~
        +remove(int) void
        +getByPriority() List~T~
        +size() int
    }

    ComplaintBox --> BaseComplaint : T extends

    %% ─── DATA STORE (SINGLETON) ──────────────────────────────────────────────
    class DataStore {
        -static DataStore instance
        +List~Citizen~ citizens
        +List~Officer~ officers
        +List~Admin~ admins
        +ComplaintBox~InfrastructureComplaint~ infraBox
        +ComplaintBox~CorruptionComplaint~ corruptionBox
        +ComplaintBox~NoiseComplaint~ noiseBox
        +ComplaintBox~TrafficComplaint~ trafficBox
        +ComplaintBox~SanitationComplaint~ sanitationBox
        +ComplaintBox~WaterSupplyComplaint~ waterSupplyBox
        +ComplaintBox~ElectricityComplaint~ electricityBox
        +ConcurrentLinkedQueue~String~ notificationQueue
        +HashMap~Integer,List~String~~ userNotifications
        +NotificationThread notificationThread
        +SessionTimeoutThread sessionTimeoutThread
        +getInstance()$ DataStore
    }

    DataStore --> ComplaintBox : contains 7 typed
    DataStore --> Citizen : stores
    DataStore --> Officer : stores
    DataStore --> Admin : stores
    DataStore --> NotificationThread : holds ref
    DataStore --> SessionTimeoutThread : holds ref

    %% ─── THREADS ─────────────────────────────────────────────────────────────
    class NotificationThread {
        -volatile boolean running
        -HashMap~Integer,Runnable~ activeCallbacks
        +registerCallback(int, Runnable) void
        +deregisterCallback(int) void
        +run() void
        +stopThread() void
    }

    class EscalationThread {
        -volatile boolean running
        -int ESCALATION_THRESHOLD
        +run() void
        +stopThread() void
    }

    class SessionTimeoutThread {
        -volatile boolean running
        +run() void
        +stopThread() void
    }

    NotificationThread --|> Thread
    EscalationThread --|> Thread
    SessionTimeoutThread --|> Thread
    EscalationThread --> DataStore : reads from
    NotificationThread --> DataStore : reads/writes
    SessionTimeoutThread --> DataStore : reads from

    %% ─── UTILITY CLASSES ─────────────────────────────────────────────────────
    class PriorityCalculator {
        +int TYPE_INFRASTRUCTURE$
        +int TYPE_CORRUPTION$
        +int TYPE_NOISE$
        +calculateScore(int,int,int)$ int
        +autoAssignStatus(int)$ Status
        +obfuscateLog(String)$ String
        +decodeLog(String)$ String
    }

    class ComplaintSearch {
        -DataStore store
        +search(int) List~BaseComplaint~
        +search(String) List~BaseComplaint~
        +search(ComplaintCategory) List~BaseComplaint~
    }

    ComplaintSearch --> DataStore : uses
    PriorityCalculator --> Status : uses

    %% ─── GUI ─────────────────────────────────────────────────────────────────
    class MainApp {
        +start(Stage) void
        +main(String[])$ void
    }

    class LoginScreen {
        +show(Stage) void
    }

    class CitizenDashboard {
        +show(Stage, Citizen) void
    }

    class OfficerDashboard {
        +show(Stage, Officer) void
    }

    class AdminDashboard {
        +show(Stage, Admin) void
    }

    MainApp --> LoginScreen : launches
    LoginScreen --> CitizenDashboard : opens
    LoginScreen --> OfficerDashboard : opens
    LoginScreen --> AdminDashboard : opens
    CitizenDashboard --> DataStore : uses
    OfficerDashboard --> DataStore : uses
    AdminDashboard --> DataStore : uses
```

---

### 2. Package Diagram

> Shows high-level package organization and inter-package dependencies.

```mermaid
graph TD
    subgraph gui["📦 gui"]
        MainApp
        LoginScreen
        CitizenDashboard
        OfficerDashboard
        AdminDashboard
    end

    subgraph users["📦 users"]
        BaseUser
        Citizen
        Officer
        Admin
    end

    subgraph complaints["📦 complaints"]
        BaseComplaint
        NoiseComplaint
        TrafficComplaint
        InfrastructureComplaint
        CorruptionComplaint
        SanitationComplaint
        WaterSupplyComplaint
        ElectricityComplaint
    end

    subgraph containers["📦 containers"]
        ComplaintBox
    end

    subgraph store["📦 store"]
        DataStore
    end

    subgraph threads["📦 threads"]
        NotificationThread
        EscalationThread
        SessionTimeoutThread
    end

    subgraph enums["📦 enums"]
        Status
        Role
        OfficerDepartment
        ComplaintCategory
    end

    subgraph exceptions["📦 exceptions"]
        DuplicateComplaintException
        ComplaintNotFoundException
        InvalidStatusTransitionException
        ComplaintExpiredException
        OfficerNotAssignedException
        UnauthorizedAccessException
    end

    subgraph priority["📦 priority"]
        PriorityCalculator
    end

    subgraph search["📦 search"]
        ComplaintSearch
    end

    subgraph profile["📦 profile"]
        CitizenProfile
    end

    gui --> users
    gui --> store
    users --> enums
    users --> profile
    users --> complaints
    users --> containers
    complaints --> enums
    complaints --> exceptions
    containers --> complaints
    containers --> exceptions
    store --> users
    store --> containers
    store --> threads
    threads --> store
    threads --> complaints
    search --> store
    search --> complaints
    priority --> enums
    profile --> exceptions
    profile --> users
```

---

### 3. Component Diagram

> Shows runtime components and how they communicate.

```mermaid
graph LR
    subgraph JavaFX_UI["🖥️ JavaFX GUI Layer"]
        UI_Login["LoginScreen"]
        UI_Citizen["CitizenDashboard"]
        UI_Officer["OfficerDashboard"]
        UI_Admin["AdminDashboard"]
    end

    subgraph Backend["⚙️ Backend Layer"]
        DS["DataStore\n(Singleton)"]
        PQ["notificationQueue\n(ConcurrentLinkedQueue)"]
        UN["userNotifications\n(HashMap)"]
    end

    subgraph Threads["🧵 Background Threads"]
        NT["NotificationThread"]
        ET["EscalationThread"]
        ST["SessionTimeoutThread"]
    end

    subgraph Logic["🧮 Business Logic"]
        CB["ComplaintBox~T~\n(Generic Container)"]
        CS["ComplaintSearch\n(Overloaded)"]
        PC["PriorityCalculator\n(Static Utility)"]
    end

    UI_Login --> DS
    UI_Citizen --> DS
    UI_Officer --> DS
    UI_Admin --> DS

    UI_Citizen -->|"registers callback"| NT
    UI_Officer -->|"registers callback"| NT

    DS --> PQ
    DS --> UN
    NT -->|"polls"| PQ
    NT -->|"writes"| UN
    ET -->|"reads all boxes"| DS
    ET -->|"pushes to"| PQ
    ST -->|"monitors session"| DS

    DS --> CB
    CB --> PC
    CS --> DS
```

---

### 4. Object Diagram

> A snapshot of real objects in memory during a typical session.

```mermaid
classDiagram
    class ds["ds : DataStore"] {
        instance = ds
    }
    class c1["c1 : Citizen"] {
        userId = 1
        username = "ram"
        password = "pass123"
        role = CITIZEN
    }
    class o1["o1 : Officer"] {
        userId = 101
        username = "officer_pwm"
        department = PWD
        assignedComplaints = 2
    }
    class a1["a1 : Admin"] {
        userId = 201
        username = "admin"
        role = ADMIN
    }
    class ic1["ic1 : InfrastructureComplaint"] {
        complaintId = 1
        title = "Pothole on MG Road"
        status = UNDER_REVIEW
        urgencyLevel = 4
        areaCode = 411001
        assignedToOfficerId = 101
    }
    class cp1["cp1 : CitizenProfile"] {
        fullName = "Ram Kumar"
        phoneNumber = "9876543210"
        homeAddress = "Pune"
    }
    class ib["infraBox : ComplaintBox"] {
        size = 2
    }

    ds --> c1 : citizens
    ds --> o1 : officers
    ds --> a1 : admins
    ds --> ib : infraBox
    ib --> ic1 : complaints
    c1 --> cp1 : profile
```

---

## 🎭 BEHAVIORAL DIAGRAMS

---

### 5. Use Case Diagram

> Shows what each actor (Citizen, Officer, Admin) can do in the system.

```mermaid
graph LR
    Citizen(("👤 Citizen"))
    Officer(("👮 Officer"))
    Admin(("🛡️ Admin"))

    subgraph System["🏛️ Civilian Complaint Portal"]
        UC1["Login"]
        UC2["File Complaint"]
        UC3["View Own Complaints"]
        UC4["Receive Notifications"]
        UC5["View Complaint Status"]
        UC6["Assign Complaint to Self"]
        UC7["Resolve Complaint"]
        UC8["Review Under-Review Complaints"]
        UC9["View All Complaints"]
        UC10["View All Citizens"]
        UC11["Search Complaints"]
        UC12["View Statistics / Priority List"]
        UC13["Auto-Escalate High Priority"]
        UC14["Session Timeout Auto-Logout"]
    end

    Citizen --> UC1
    Citizen --> UC2
    Citizen --> UC3
    Citizen --> UC4
    Citizen --> UC5

    Officer --> UC1
    Officer --> UC6
    Officer --> UC7
    Officer --> UC8
    Officer --> UC4

    Admin --> UC1
    Admin --> UC9
    Admin --> UC10
    Admin --> UC11
    Admin --> UC12

    EscalationThread(("⚙️ EscalationThread")) --> UC13
    SessionThread(("⚙️ SessionThread")) --> UC14
```

---

### 6. Sequence Diagram — Login Flow

> Shows the message flow when a user logs in.

```mermaid
sequenceDiagram
    actor User
    participant LoginScreen
    participant DataStore
    participant BaseUser
    participant Dashboard

    User->>LoginScreen: enter username + password
    LoginScreen->>DataStore: getInstance()
    DataStore-->>LoginScreen: singleton instance
    LoginScreen->>DataStore: search citizens/officers/admins
    DataStore-->>LoginScreen: user list
    LoginScreen->>BaseUser: login(username, password)
    BaseUser-->>LoginScreen: true / false

    alt Login Successful
        LoginScreen->>DataStore: start SessionTimeoutThread
        LoginScreen->>Dashboard: show(stage, user)
        Dashboard->>DataStore: notificationThread.registerCallback(userId, bell)
    else Login Failed
        LoginScreen-->>User: show "Invalid credentials" alert
    end
```

---

### 7. Sequence Diagram — File a Complaint

> Shows flow from citizen clicking "File Complaint" to saving in DataStore.

```mermaid
sequenceDiagram
    actor Citizen
    participant CitizenDashboard
    participant DataStore
    participant ComplaintBox
    participant BaseComplaint
    participant PriorityCalculator
    participant NotificationThread

    Citizen->>CitizenDashboard: fill form + click Submit
    CitizenDashboard->>PriorityCalculator: calculateScore(type, urgency, areaCode)
    PriorityCalculator-->>CitizenDashboard: priorityScore

    CitizenDashboard->>DataStore: getInstance()
    CitizenDashboard->>BaseComplaint: new XxxComplaint(id, title, desc, ...)
    BaseComplaint-->>CitizenDashboard: complaint object (status=FILED)

    CitizenDashboard->>ComplaintBox: addComplaint(complaint)

    alt Duplicate within 24h
        ComplaintBox-->>CitizenDashboard: throw DuplicateComplaintException
        CitizenDashboard-->>Citizen: show error alert
    else No Duplicate
        ComplaintBox->>ComplaintBox: complaints.add(complaint)
        CitizenDashboard->>DataStore: notificationQueue.offer("USERID:X|MSG:Filed")
        DataStore->>NotificationThread: poll queue
        NotificationThread-->>CitizenDashboard: invoke bellUpdateCallback
        CitizenDashboard-->>Citizen: show success + refresh table
    end
```

---

### 8. Sequence Diagram — Officer Resolves a Complaint

> Shows the status state machine validation during resolution.

```mermaid
sequenceDiagram
    actor Officer
    participant OfficerDashboard
    participant DataStore
    participant BaseComplaint
    participant NotificationThread

    Officer->>OfficerDashboard: select complaint + click "Resolve"
    OfficerDashboard->>DataStore: get complaint from box
    DataStore-->>OfficerDashboard: complaint object

    OfficerDashboard->>BaseComplaint: assignOfficer(officerId, requestingOfficerId)

    alt Officer not assigned
        BaseComplaint-->>OfficerDashboard: throw OfficerNotAssignedException
        OfficerDashboard-->>Officer: show "Not authorized" alert
    else Officer is assigned
        OfficerDashboard->>BaseComplaint: updateStatus(RESOLVED)

        alt Invalid transition (e.g., FILED → RESOLVED)
            BaseComplaint-->>OfficerDashboard: throw InvalidStatusTransitionException
            OfficerDashboard-->>Officer: show "Invalid status transition" alert
        else Already RESOLVED or REJECTED
            BaseComplaint-->>OfficerDashboard: throw ComplaintExpiredException
            OfficerDashboard-->>Officer: show "Complaint already closed" alert
        else Valid UNDER_REVIEW → RESOLVED
            BaseComplaint-->>OfficerDashboard: status = RESOLVED
            OfficerDashboard->>DataStore: notificationQueue.offer("USERID:citizenId|MSG:Resolved")
            DataStore->>NotificationThread: poll queue
            NotificationThread-->>OfficerDashboard: invoke citizen bell callback
            OfficerDashboard-->>Officer: refresh table, show success
        end
    end
```

---

### 9. Sequence Diagram — Auto-Escalation (Background)

> Shows how EscalationThread works independently every 10 seconds.

```mermaid
sequenceDiagram
    participant EscalationThread
    participant DataStore
    participant BaseComplaint
    participant NotificationThread

    loop Every 10 seconds
        EscalationThread->>DataStore: collectAllComplaints() from 7 boxes
        DataStore-->>EscalationThread: List of all complaints

        loop For each FILED or UNDER_REVIEW complaint
            EscalationThread->>BaseComplaint: calculatePriorityScore()
            BaseComplaint-->>EscalationThread: score

            alt score > ESCALATION_THRESHOLD (14)
                EscalationThread->>BaseComplaint: autoEscalate()
                BaseComplaint-->>EscalationThread: status = ESCALATED
                EscalationThread->>DataStore: notificationQueue.offer("USERID:X|MSG:Escalated")
                DataStore->>NotificationThread: queue polled next cycle
                NotificationThread-->>DataStore: stores in userNotifications
            end
        end

        EscalationThread->>EscalationThread: Thread.sleep(10000)
    end
```

---

### 10. Activity Diagram — Complaint Lifecycle

> Shows the full journey of a complaint from filing to closure.

```mermaid
flowchart TD
    START(["🟢 Citizen Files Complaint"]) --> DUP{Duplicate within 24h?}
    DUP -->|Yes| ERR1["❌ DuplicateComplaintException\nComplaint rejected"]
    DUP -->|No| FILED["📥 Status: FILED\nSaved in ComplaintBox"]

    FILED --> PRIORITY["⚙️ PriorityCalculator\ncalculates score"]
    PRIORITY --> SCORE{Priority Score?}

    SCORE -->|"> 14 (Auto)"| AUTO_ESC["🔺 EscalationThread\nautoEscalate()"]
    AUTO_ESC --> ESCALATED_ST["🔴 Status: ESCALATED\nNotification sent to Citizen"]

    SCORE -->|"≤ 14"| OFFICER_ACT{Officer Action?}
    OFFICER_ACT -->|"Review"| UNDER_REVIEW["🔵 Status: UNDER_REVIEW\nOfficer assigned"]

    UNDER_REVIEW --> OFFICER_DEC{Officer Decision?}
    OFFICER_DEC -->|"Resolve"| CHECK_AUTH{Is officer assigned?}
    CHECK_AUTH -->|No| ERR2["❌ OfficerNotAssignedException"]
    CHECK_AUTH -->|Yes| RESOLVED["✅ Status: RESOLVED\nNotification to Citizen"]

    OFFICER_DEC -->|"Escalate"| ESCALATED_ST
    OFFICER_DEC -->|"Reject"| REJECTED["🚫 Status: REJECTED"]

    ESCALATED_ST --> OFFICER_DEC2{Higher Auth Action?}
    OFFICER_DEC2 -->|"Resolve"| RESOLVED
    OFFICER_DEC2 -->|"Return to Review"| UNDER_REVIEW

    RESOLVED --> ARCHIVE{"> 30 days old?"}
    ARCHIVE -->|Yes| LOCKED["🔒 Archived — cannot be removed"]
    ARCHIVE -->|No| REMOVABLE["🗑️ Can be removed"]

    RESOLVED --> END_R(["🔴 Closed"])
    REJECTED --> END_J(["🔴 Closed"])
    LOCKED --> END_L(["🔒 Archived"])
```

---

### 11. State Machine Diagram — Complaint Status

> Shows all valid and invalid transitions for a complaint's Status.

```mermaid
stateDiagram-v2
    [*] --> FILED : Citizen files complaint

    FILED --> UNDER_REVIEW : Officer reviews
    FILED --> REJECTED : Officer rejects at filing

    UNDER_REVIEW --> RESOLVED : Officer resolves
    UNDER_REVIEW --> ESCALATED : Officer escalates / auto-escalation
    UNDER_REVIEW --> REJECTED : Officer rejects

    ESCALATED --> UNDER_REVIEW : Higher authority de-escalates
    ESCALATED --> RESOLVED : Higher authority resolves

    RESOLVED --> [*] : Terminal state\n(locked after 30 days)
    REJECTED --> [*] : Terminal state

    note right of RESOLVED
        ComplaintExpiredException thrown
        if modification attempted
    end note

    note right of REJECTED
        ComplaintExpiredException thrown
        if modification attempted
    end note
```

---

### 12. Collaboration (Communication) Diagram

> Shows which objects communicate with each other and the nature of the relationship.

```mermaid
graph TD
    MainApp -->|"1. launches"| LoginScreen
    LoginScreen -->|"2. authenticates via"| DataStore
    DataStore -->|"3. returns user"| LoginScreen
    LoginScreen -->|"4. opens"| CitizenDashboard
    LoginScreen -->|"4. opens"| OfficerDashboard
    LoginScreen -->|"4. opens"| AdminDashboard

    CitizenDashboard -->|"5. fileComplaint()"| Citizen
    Citizen -->|"6. addComplaint()"| ComplaintBox
    ComplaintBox -->|"7. validates with"| BaseComplaint
    BaseComplaint -->|"8. score via"| PriorityCalculator

    OfficerDashboard -->|"9. resolveComplaint()"| Officer
    Officer -->|"10. updateStatus()"| BaseComplaint

    BaseComplaint -->|"11. notifies via"| DataStore
    DataStore -->|"12. queues message"| NotificationThread
    NotificationThread -->|"13. invokes callback"| CitizenDashboard

    EscalationThread -->|"A. reads"| DataStore
    EscalationThread -->|"B. autoEscalate()"| BaseComplaint
    EscalationThread -->|"C. queues alert"| DataStore

    SessionTimeoutThread -->|"D. monitors login time"| DataStore
    SessionTimeoutThread -->|"E. forces logout"| LoginScreen
```

---

## 📋 Summary Table

| # | Diagram Type | Category | What It Shows |
|---|---|---|---|
| 1 | **Class Diagram** | Structural | All classes, fields, methods, inheritance, generics |
| 2 | **Package Diagram** | Structural | 11 packages + inter-package dependencies |
| 3 | **Component Diagram** | Structural | Runtime components (GUI, Backend, Threads, Logic) |
| 4 | **Object Diagram** | Structural | Snapshot of real objects during a session |
| 5 | **Use Case Diagram** | Behavioral | What Citizen / Officer / Admin can do |
| 6 | **Sequence: Login** | Behavioral | Login authentication message flow |
| 7 | **Sequence: File Complaint** | Behavioral | Filing + duplicate check + notification |
| 8 | **Sequence: Resolve** | Behavioral | Officer resolving with full exception handling |
| 9 | **Sequence: Auto-Escalation** | Behavioral | EscalationThread background loop |
| 10 | **Activity Diagram** | Behavioral | Full complaint lifecycle end-to-end |
| 11 | **State Machine** | Behavioral | Valid/invalid Status transitions |
| 12 | **Collaboration Diagram** | Behavioral | Object communication map |

---

## 🎯 OOP Concepts Mapped to Diagrams

| OOP Concept | Seen In |
|---|---|
| **Inheritance** | Class Diagram → BaseUser ← Citizen/Officer/Admin; BaseComplaint ← 7 subclasses |
| **Polymorphism** | Class Diagram → `calculatePriorityScore()` overridden in each complaint; `performAction()` overridden |
| **Encapsulation** | Class Diagram → `CitizenProfile` private fields; `ComplaintBox` private list |
| **Abstraction** | Class Diagram → `BaseUser`, `BaseComplaint` are abstract |
| **Generics** | Class Diagram → `ComplaintBox<T extends BaseComplaint>` |
| **Method Overloading** | Class Diagram → `search(int)`, `search(String)`, `search(ComplaintCategory)` |
| **Multithreading** | Component, Sequence, Collaboration → `NotificationThread`, `EscalationThread`, `SessionTimeoutThread` |
| **Exception Handling** | Sequence Diagrams (Login, Resolve, File) → 6 custom exceptions thrown and caught |
| **Singleton Pattern** | Class + Component → `DataStore.getInstance()` |
