# ArchGuard — Developer & User Manual

> **Automated Architecture Governance & Code Quality Gate for Modern Java & Spring Boot Applications.**

---

## Table of Contents
1. [Overview](#1-overview)
2. [Prerequisites & System Requirements](#2-prerequisites--system-requirements)
3. [Quick Start Guide](#3-quick-start-guide)
4. [Maven Plugin Configuration](#4-maven-plugin-configuration)
5. [Core Commands & Execution](#5-core-commands--execution)
6. [Baseline & Technical Debt Ratcheting](#6-baseline--technical-debt-ratcheting)
7. [Comprehensive Rule Catalog (AG001 – AG029)](#7-comprehensive-rule-catalog-ag001--ag029)
8. [In-Code Rule Suppressions](#8-in-code-rule-suppressions)
9. [Configuration File Reference (`archguard.yml`)](#9-configuration-file-reference-archguardyml)
10. [Reports & Diagnostics](#10-reports-diagnostics--automated-pr-code-review)
11. [CI/CD Pipeline Integration](#11-cicd-pipeline-integration)
12. [Troubleshooting & FAQ](#12-troubleshooting--faq)

---

## 1. Overview

**ArchGuard** (`com.architectureguard:architecture-guard-maven-plugin`) is a static analysis and quality gate tool designed specifically for Java 21+ and Spring Boot applications.

Unlike traditional linters that only inspect syntax or basic code smells, ArchGuard combines:
- **High-performance Bytecode Analysis (ASM):** Inspects compiled class files to analyze type hierarchies, method calls, field types, and class-level layer dependencies.
- **Precision AST Source Inspection (JavaParser):** Scans source code for semantic smells such as direct entity leakage in controller APIs, mutable static collections, field-level `@Autowired` injections, swallowed exception blocks, and hardcoded credentials.
- **Non-Intrusive Quality Gates with Baselining:** Protects existing legacy projects from developer frustration by freezing existing violations in a baseline snapshot file and only failing the build on **newly introduced** violations.

```mermaid
flowchart TD
    A["Java 21 Source (.java)"] -->|javac| B["Compiled Classes (.class)"]
    A --> C["AST Source Analyzer (JavaParser)"]
    B --> D["Bytecode Analyzer (ASM)"]
    C --> E["ArchGuard Engine"]
    D --> E
    E --> F{"Baseline Snapshot (.json) Present?"}
    F -->|Yes| G["Multiset Differential Engine\n(NEW vs BASELINED vs RESOLVED)"]
    F -->|No| H["All Violations Evaluated"]
    G --> I{"New Violations >= failOnSeverity?"}
    H --> J{"Total Violations >= failOnSeverity?"}
    I -->|Yes| K["BUILD FAILURE ❌"]
    I -->|No| L["BUILD SUCCESS ✅"]
    J -->|Yes| K
    J -->|No| L
    G --> M["HTML / JSON / Console Reports"]
    H --> M
```

---

## 2. Prerequisites & System Requirements

- **JDK:** Java 21 or higher
- **Build Tool:** Apache Maven 3.8.0 or higher
- **Frameworks Supported:** Spring Boot 3.x, Spring Framework 6.x, Standard Java applications

---

## 3. Quick Start Guide

### Option A: Interactive Quickstart (Recommended)

Run the `init` mojo directly in your project root to auto-detect base packages and scaffold a production-ready `archguard.yml`:

```bash
mvn com.architectureguard:architecture-guard-maven-plugin:1.0.0:init
```

### Option B: Manual Setup

Add the `architecture-guard-maven-plugin` to your project's `<build><plugins>` section in `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.architectureguard</groupId>
            <artifactId>architecture-guard-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!-- Default failure threshold is ERROR. Set to WARNING to fail on all code smells -->
                <failOnSeverity>ERROR</failOnSeverity>
                <outputDirectory>${project.build.directory}/architecture-guard</outputDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Step 2: Run Analysis

```bash
mvn clean verify
```

ArchGuard will compile your classes, evaluate all 25 architecture and quality rules, generate offline interactive HTML, JSON, and standard SARIF 2.1.0 reports, and fail the build if violations at or above the threshold are detected.

---

## 4. Maven Plugin Configuration

### Full `<configuration>` Options

| Parameter | Type | Default | CLI Property | Description |
| :--- | :--- | :--- | :--- | :--- |
| `failOnSeverity` | `String` | `ERROR` | `-Darchguard.failOnSeverity` | Minimum severity that triggers a build failure (`ERROR`, `WARNING`, `INFO`, `NONE`). |
| `failOnNewViolationsOnly` | `boolean` | `true` | `-Darchguard.failOnNewViolationsOnly` | When a baseline file exists, only fail the build if new violations are introduced. |
| `ratchet` | `boolean` | `false` | `-Darchguard.ratchet` | Automatically update baseline file when legacy debt is resolved (ratchet mechanism). |
| `baselineFile` | `File` | `${project.basedir}/.architecture-guard-baseline.json` | `-Darchguard.baselineFile` | Custom location for the baseline snapshot file. |
| `configFile` | `File` | (auto-discovered) | `-Darchguard.configFile` | Path to custom `archguard.yml` or `architecture-guard.json`. |
| `outputDirectory` | `File` | `${project.build.directory}/architecture-guard` | `-Darchguard.outputDirectory` | Target directory for generated HTML, JSON, and SARIF reports. |
| `skip` | `boolean` | `false` | `-Darchguard.skip` | Skips ArchGuard analysis execution. |

---

## 5. Core Commands & Execution

### 1. Initialize ArchGuard in Any Project
Auto-detects base packages and creates `archguard.yml`:
```bash
mvn architecture-guard:init
```

### 2. Run Standard Quality Gate
Runs the analysis during the `verify` phase (or manually):
```bash
mvn clean compile architecture-guard:check
```

### 3. Generate a Baseline Snapshot for Existing Projects
Freezes all current project debt into `.architecture-guard-baseline.json`:
```bash
mvn clean compile architecture-guard:baseline
```

### 4. Run with Ratchet Mode Enabled
Ensures baseline shrinks automatically as developers fix old violations:
```bash
mvn clean verify -Darchguard.ratchet=true
```

### 5. Enforce Strict Warning Failures
Fails the build if any new `WARNING` (such as `System.out.println` or `@Autowired`) is added:
```bash
mvn clean verify -Darchguard.failOnSeverity=WARNING
```

---

## 6. Baseline & Technical Debt Ratcheting

### The Problem: Legacy Code Fatigue
When introducing architectural tooling into large existing projects with thousands of pre-existing violations, developers get blocked and frustrated by legacy violations they did not write.

### The Solution: Quality Gate via Differential Baselining
ArchGuard allows tech leads or CI pipelines to freeze legacy debt:

```bash
mvn architecture-guard:baseline
```

This generates a version-controlled `.architecture-guard-baseline.json` file containing SHA-256 fingerprints of every current violation.

#### How It Works:
1. **Existing violations** are tagged as `BASELINED` in reports and **do not fail the build**.
2. **New violations** introduced in PRs/branches are tagged as `NEW` and **fail the build**.
3. **Fixed violations** are counted as `RESOLVED`.
4. **Multiset Protection:** If a class already has 1 baselined `System.out.println`, adding a second `System.out.println` is correctly detected as `NEW` (frequency tracking).

#### Automatic Ratchet Mode:
Enable `<ratchet>true</ratchet>` or `-Darchguard.ratchet=true`. Whenever a developer fixes a legacy violation, ArchGuard automatically removes it from `.architecture-guard-baseline.json`. Debt can only decrease, never increase.

---

## 7. Comprehensive Rule Catalog (AG001 – AG027)

### [AG001] Controller to Repository Direct Dependency
- **Severity:** `ERROR`
- **Rationale:** Web controllers must not bypass the business service layer to directly access repositories. Bypassing causes transaction leaks, duplicate business logic, and tight coupling.
- ❌ **Violation:**
  ```java
  @RestController
  public class OrderController {
      @Autowired
      private OrderRepository orderRepository; // Direct DB access from Controller!
  }
  ```
- ✅ **Remediation:**
  ```java
  @RestController
  public class OrderController {
      private final OrderService orderService;
      public OrderController(OrderService orderService) {
          this.orderService = orderService;
      }
  }
  ```

---

### [AG002] Repository to Service Inverted Dependency
- **Severity:** `ERROR`
- **Rationale:** Persistence layer components must remain low-level data adapters and never depend on high-level service orchestration classes.

---

### [AG003] Domain Model Outer Layer Dependency
- **Severity:** `ERROR`
- **Rationale:** Core domain entities and domain services must remain pure business logic and never depend on web, infrastructure, or controller components.

---

### [AG004] Circular Dependencies
- **Severity:** `ERROR`
- **Rationale:** Package-level and class-level cyclic dependencies (e.g., Service A $\rightarrow$ Service B $\rightarrow$ Service A) create tight coupling, fragile refactoring, and Spring context initialization deadlocks.
- ✅ **Remediation:** Extract shared logic into a common domain service or introduce an event-driven decoupled architecture.

---

### [AG005] Package Boundary Cross-Access
- **Severity:** `ERROR`
- **Rationale:** Enforces modular boundaries between bounded contexts (e.g., `com.example.billing` cannot directly access internal classes of `com.example.inventory`).

---

### [AG006] Spring Component in Domain Package
- **Severity:** `WARNING`
- **Rationale:** Framework-specific annotations (e.g., `@Service`, `@Component`, `@RestController`) should not pollute pure domain packages.

---

### [AG007] Field Injection (`@Autowired`)
- **Severity:** `WARNING`
- **Rationale:** Field injection hides dependencies, prevents immutability (`final` fields), complicates unit testing without reflection, and masks circular dependencies.
- ❌ **Violation:**
  ```java
  @Service
  public class PaymentService {
      @Autowired
      private PaymentGateway gateway;
  }
  ```
- ✅ **Remediation:**
  ```java
  @Service
  public class PaymentService {
      private final PaymentGateway gateway;
      public PaymentService(PaymentGateway gateway) {
          this.gateway = gateway;
      }
  }
  ```

---

### [AG008] Swallowed Exception
- **Severity:** `WARNING`
- **Rationale:** Empty `catch` blocks or catching exceptions without logging or rethrowing cause silent failures in production that are nearly impossible to diagnose.
- ❌ **Violation:**
  ```java
  try {
      doCriticalWork();
  } catch (Exception e) {
      // Empty catch block!
  }
  ```
- ✅ **Remediation:**
  ```java
  try {
      doCriticalWork();
  } catch (Exception e) {
      log.error("Failed to process critical work", e);
      throw new BusinessProcessingException("Operation failed", e);
  }
  ```

---

### [AG009] Missing `@Transactional` on Multi-Repository Operations
- **Severity:** `WARNING`
- **Rationale:** Public service methods performing multiple database write/update operations across repositories without a transaction boundary risk partial commits. If the second repository call fails, the first write remains committed in the database, causing silent data corruption and inconsistent state.
- ❌ **Violation (Missing Transaction Boundary):**
  ```java
  @Service
  public class OrderProcessingService {
      private final OrderRepository orderRepository;
      private final AuditRepository auditRepository;

      // Missing @Transactional! If auditRepository fails, order remains saved!
      public void completeOrder(Order order, AuditRecord audit) {
          orderRepository.save(order);
          auditRepository.record(audit);
      }
  }
  ```
- ✅ **Standard Remediation:**
  ```java
  @Service
  public class OrderProcessingService {
      private final OrderRepository orderRepository;
      private final AuditRepository auditRepository;

      @Transactional // Guarantees both calls succeed or both roll back together
      public void completeOrder(Order order, AuditRecord audit) {
          orderRepository.save(order);
          auditRepository.record(audit);
      }
  }
  ```

#### 💡 Architectural Pattern: Handling Remote HTTP / 3rd-Party I/O with Multi-Repo Writes
> **⚠️ Critical HikariCP Performance Warning:** Never place slow remote HTTP calls (e.g. Stripe, REST clients, Feign) **inside** a `@Transactional` method. Doing so locks the database connection for seconds while waiting for network I/O, quickly exhausting the database connection pool.

To satisfy atomic multi-repository writes without locking database connections during remote I/O, use one of these two enterprise patterns:

**Pattern A: Programmatic `TransactionTemplate` (Recommended for Co-located Logic)**
```java
@Service
public class OrderCheckoutService {
    private final OrderRepository orderRepository;
    private final AuditRepository auditRepository;
    private final RestClient paymentClient;
    private final TransactionTemplate transactionTemplate;

    // Method itself is NOT @Transactional (does not hold DB connection during HTTP call)
    public void checkout(OrderRequest request) {
        // 1. Remote HTTP call executed with ZERO DB connections locked
        PaymentResult payment = paymentClient.post().uri("/pay").retrieve().body(PaymentResult.class);

        // 2. Atomic multi-repository write executed inside dedicated transaction boundary
        transactionTemplate.executeWithoutResult(status -> {
            orderRepository.save(new Order(request, payment));
            auditRepository.record(new AuditRecord(request.id(), payment));
        });
    }
}
```

**Pattern B: The Orchestrator Pattern (Recommended for Hexagonal / Clean Architecture)**
Separate orchestration from database persistence:
```java
// 1. Non-transactional Orchestrator: Coordinates network I/O
@Service
public class OrderOrchestrator {
    private final RestClient paymentClient;
    private final OrderPersistenceService persistenceService;

    public void checkout(OrderRequest request) {
        PaymentResult payment = paymentClient.post().uri("/pay").retrieve().body(PaymentResult.class);
        persistenceService.saveOrderAndAudit(request, payment); // Delegates to atomic DB service
    }
}

// 2. Dedicated Transactional Service: Handles atomic repository mutations
@Service
public class OrderPersistenceService {
    private final OrderRepository orderRepository;
    private final AuditRepository auditRepository;

    @Transactional // Satisfies AG009 without holding connections during remote I/O
    public void saveOrderAndAudit(OrderRequest request, PaymentResult payment) {
        orderRepository.save(new Order(request, payment));
        auditRepository.record(new AuditRecord(request.id(), payment));
    }
}
```

---

### [AG010] Unsafe Mutable Static State
- **Severity:** `ERROR`
- **Rationale:** Non-final static fields or mutable static collections (`static List`, `static Map`) in concurrent server environments cause race conditions, cross-request memory leaks, and non-deterministic bugs.
- ❌ **Violation:**
  ```java
  public class UserSessionCache {
      public static List<User> activeUsers = new ArrayList<>(); // Unsafe shared state!
  }
  ```
- ✅ **Remediation:** Use thread-safe Spring beans (e.g., singleton service with `ConcurrentHashMap` or an external cache like Redis).

---

### [AG011] Direct Console Logging
- **Severity:** `WARNING`
- **Rationale:** Direct calls to `System.out.println()`, `System.err.println()`, or `e.printStackTrace()` block I/O threads, cannot be filtered by log level in production, and bypass central log aggregators (e.g., ELK / Datadog).
- ❌ **Violation:**
  ```java
  System.out.println("Processing order: " + orderId);
  e.printStackTrace();
  ```
- ✅ **Remediation:**
  ```java
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  // Or Lombok: @Slf4j
  log.info("Processing order: {}", orderId);
  log.error("Order processing failed", e);
  ```

---

### [AG012] Direct Entity Exposure in REST API
- **Severity:** `ERROR`
- **Rationale:** Exposing database JPA `@Entity` classes directly in `@RestController` request/response signatures causes mass-assignment security vulnerabilities, unintended DB modifications, lazy loading serialization exceptions (`LazyInitializationException`), and exposes DB schema details to API consumers.
- ❌ **Violation:**
  ```java
  @GetMapping("/users/{id}")
  public UserEntity getUser(@PathVariable Long id) { // Exposing JPA entity!
      return userService.findById(id);
  }
  ```
- ✅ **Remediation:** Use Java 21 Records or DTOs:
  ```java
  public record UserResponse(Long id, String name, String email) {}

  @GetMapping("/users/{id}")
  public UserResponse getUser(@PathVariable Long id) {
      return userService.getUserDto(id);
  }
  ```

---

### [AG013] Hardcoded Secrets Scanner
- **Severity:** `ERROR`
- **Rationale:** Committing plaintext passwords, database credentials, AWS access keys, or JWT private keys directly into Java source code is a major security risk.
- ❌ **Violation:**
  ```java
  private String apiKey = "AKIAIOSFODNN7EXAMPLE";
  private String dbPassword = "password1234!";
  ```
- ✅ **Remediation:** Load credentials from environment variables or Spring Cloud Vault / AWS Secrets Manager:
  ```java
  @Value("${app.security.api-key}")
  private String apiKey;
  ```

---

### [AG014] JPA Eager Fetch / N+1 Query Risk
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Using `FetchType.EAGER` in JPA relationship mappings causes Cartesian explosion in SQL queries, excessive memory consumption, and N+1 query performance degradation.
- ❌ **Violation:**
  ```java
  @Entity
  public class OrderEntity {
      @OneToMany(fetch = FetchType.EAGER)
      private List<OrderItemEntity> items; // Eager load causes Cartesian product queries!
  }
  ```
- ✅ **Remediation:**
  ```java
  @Entity
  public class OrderEntity {
      @OneToMany(fetch = FetchType.LAZY)
      private List<OrderItemEntity> items;
  }
  ```

---

### [AG015] Missing `@Valid` on REST `@RequestBody`
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Accepting mutation payloads without `@Valid` or `@Validated` bypasses Bean Validation annotations (`@NotNull`, `@Size`, `@Pattern`), allowing unvalidated data to reach domain logic.
- ❌ **Violation:**
  ```java
  @PostMapping("/users")
  public UserResponse createUser(@RequestBody CreateUserRequest request) { // Missing @Valid!
      return userService.create(request);
  }
  ```
- ✅ **Remediation:**
  ```java
  @PostMapping("/users")
  public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
      return userService.create(request);
  }
  ```

---

### [AG016] Unpaginated Collection in REST Controller
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Returning raw unpaginated `List<T>` or `Set<T>` from GET endpoints risks OutOfMemoryError and severe database latency as datasets scale.
- ❌ **Violation:**
  ```java
  @GetMapping("/orders")
  public List<OrderResponse> getAllOrders() { // Unpaginated list!
      return orderService.findAll();
  }
  ```
- ✅ **Remediation:**
  ```java
  @GetMapping("/orders")
  public Page<OrderResponse> getAllOrders(Pageable pageable) {
      return orderService.findAll(pageable);
  }
  ```

---

### [AG017] Injected Dependency Bloat / God Class
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Classes with $\ge 7$ injected dependencies indicate a violation of the Single Responsibility Principle, creating tightly coupled God Classes that are difficult to test and maintain.
- ✅ **Remediation:** Refactor into smaller domain services or introduce Facade / Strategy patterns to reduce collaborator count.

---

### [AG018] High Method Cyclomatic & Cognitive Complexity
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Methods with cyclomatic complexity $> 15$ contain excessive conditional branching (`if/else`, `switch`, loops, ternary operators), leading to high cognitive load, elevated defect rates, and difficult unit testing.
- ❌ **Violation:** Monolithic 120-line methods with 5 nested `if/for` branches and complex switch statements.
- ✅ **Remediation:** Extract independent logic into private single-responsibility helper methods or replace branching with a Polymorphic Strategy pattern.

---

### [AG019] Algorithmic Performance Risk ($O(n^2)$ Lookups & DB in Loops)
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Executing linear collection searches (`List.contains()`, `List.indexOf()`) or remote database calls (`repository.findById()`) inside iteration loops causes quadratic $O(n^2)$ CPU bottlenecks or N+1 remote database roundtrip latency.
- ❌ **Violation:**
  ```java
  for (Order order : orderList) {
      if (processedOrderIds.contains(order.getId())) { // O(n*m) scan on List!
          userRepository.findById(order.getUserId()); // N+1 Database queries in loop!
      }
  }
  ```
- ✅ **Remediation:** Convert lookup collections to `HashSet` for $O(1)$ lookups and batch database queries:
  ```java
  Set<Long> processedSet = new HashSet<>(processedOrderIds); // O(1) set
  List<User> users = userRepository.findAllById(userIds); // Single batch query!
  ```

---

### [AG020] Code Readability & Clean Code Antipatterns
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Poorly structured, oversized classes and complex nested methods dramatically increase cognitive load, make peer reviews error-prone, and indicate God Class / Spaghetti Code smells:
  - **Bloated Class/File ($> 450$ lines):** Indicates multiple responsibilities crammed into a single file.
  - **Excessive Method Count ($> 25$ methods):** High public surface area and low cohesion.
  - **Monster Method ($> 60$ lines):** Violates Single Responsibility and does too much.
  - **Long Parameter List ($\ge 5$ parameters):** Parameter explosion and tight coupling.
  - **Deep Nesting / Arrow Antipattern ($\ge 4$ levels):** Deeply nested `if/for/try` blocks impair readability.
- ❌ **Violation:**
  ```java
  public void processBatch(String id, String type, int priority, boolean retry, List<String> tags) {
      if (id != null) {
          for (String tag : tags) {
              if (tag.startsWith("X")) {
                  try {
                      // Level 4+ nesting, 75 lines long!
                  } catch (Exception e) { ... }
              }
          }
      }
  }
  ```
- ✅ **Remediation:**
  1. Use **Guard Clauses (Return Early)**: `if (id == null) return;` to flatten indentation.
  2. Encapsulate long parameter lists into a Java 21 `record RequestParams(...)`.
  3. Extract cohesive sub-blocks into descriptive private helper methods (`extractTagData()`, `validateBatch()`).
  4. Decompose bloated classes into focused domain components.

---

### [AG021] Spring Self-Invocation Proxy Bypass Detection
- **Severity:** `ERROR`
- **Rationale:** In Spring, proxy-driven features (`@Transactional`, `@Async`, `@Cacheable`, `@CacheEvict`, `@Retryable`, `@Scheduled`) work by intercepting calls through an AOP proxy bean. When a method calls another annotated method in the **same class** via `this.method()` or direct call, the call bypasses the Spring proxy entirely, silently executing without transaction management, asynchronous threading, or cache interception!
- ❌ **Violation:**
  ```java
  @Service
  public class OrderProcessingService {

      public void processOrderBatch(List<Order> orders) {
          for (Order order : orders) {
              // Self-invocation: calls @Transactional method internally bypassing Spring proxy!
              saveOrderWithAudit(order);
          }
      }

      @Transactional
      public void saveOrderWithAudit(Order order) {
          orderRepository.save(order);
          auditRepository.record(order);
      }
  }
  ```
- ✅ **Remediation:**
  1. **Extract to a separate Spring Bean:** Move the transactional/async method to a dedicated helper service (e.g. `OrderTransactionService`).
  2. **Inject Self-Proxy / ObjectProvider:** Inject `ObjectProvider<OrderProcessingService>` or self-reference.
  3. **Use TransactionTemplate:** Use programmatic `TransactionTemplate.execute(...)` instead of method-level `@Transactional`.

---

### [AG022] Singleton Spring Bean Mutable Instance State
- **Severity:** `ERROR`
- **Rationale:** Spring beans (`@Service`, `@Component`, `@RestController`, `@Repository`) are singletons by default and shared across concurrent HTTP request threads. Defining non-final mutable fields, mutable collections, or non-thread-safe formatters (`SimpleDateFormat`, `Calendar`, `DecimalFormat`) creates severe multithreaded race conditions, corrupted user session state, and cross-request data leaks.
- ❌ **Violation:**
  ```java
  @Service
  public class OrderProcessingService {
      private List<Order> currentBatch = new ArrayList<>(); // Shared state across all HTTP requests!
      private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Not thread-safe!
      private int processedCount = 0; // Multithreaded race condition!
  }
  ```
- ✅ **Remediation:**
  1. Make fields `final` and immutable (or inject stateless Spring beans).
  2. Scope state to method execution (thread-local on call stack).
  3. Replace `SimpleDateFormat` with thread-safe `java.time.format.DateTimeFormatter`.

---

### [AG023] NullPointer Traps & Defensive Coding Antipatterns
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Detects high-probability NullPointer bugs and defensive coding anti-patterns:
  - **Raw `Optional.get()` without check:** Calling `findUser().get()` without `.isPresent()` throws `NoSuchElementException`.
  - **Returning `null` Collection / Optional:** Returning `return null;` from a method with return type `List<T>`, `Set<T>`, `Map<K,V>`, or `Optional<T>` forces callers to do manual null checks or crash.
  - **Inverted Literal Equals:** `variable.equals("ADMIN")` throws `NullPointerException` if `variable` is null.
- ❌ **Violation:**
  ```java
  public List<Order> getOrders(String status) {
      if (status.equals("COMPLETED")) { // Throws NPE if status is null!
          User user = userRepository.findById(1L).get(); // Throws NoSuchElementException if absent!
          return null; // Forces callers to null-check collections!
      }
      return orderService.findAll();
  }
  ```
- ✅ **Remediation:**
  ```java
  public List<Order> getOrders(String status) {
      if ("COMPLETED".equals(status)) { // Null-safe Yoda equality
          User user = userRepository.findById(1L).orElseThrow(() -> new UserNotFoundException(1L));
          return Collections.emptyList(); // Safe empty collection
      }
      return orderService.findAll();
  }
  ```

---

### [AG024] Unclosed AutoCloseable Resource / I/O Stream Leaks
- **Severity:** `ERROR`
- **Rationale:** Instantiating `AutoCloseable` I/O streams, readers, or database resources (`FileInputStream`, `FileOutputStream`, `BufferedReader`, `Connection`, `PreparedStatement`, `ResultSet`, `Scanner`) outside of a **try-with-resources** statement leads to leaked operating system file descriptors and exhausted database connection pools.
- ❌ **Violation:**
  ```java
  public void exportData(File file) throws IOException {
      FileInputStream fis = new FileInputStream(file); // Leaks file handle if exception occurs!
      byte[] data = fis.readAllBytes();
      fis.close();
  }
  ```
- ✅ **Remediation:**
  ```java
  public void exportData(File file) throws IOException {
      try (FileInputStream fis = new FileInputStream(file)) { // Guaranteed automatic cleanup!
          byte[] data = fis.readAllBytes();
      }
  }
  ```

---

### [AG025] Inefficient String Concatenation in Loops
- **Severity:** `WARNING` (Advisory)
- **Rationale:** Repeated string concatenation using `+` or `+=` inside loops (`for`, `while`, `do-while`) creates intermediate `String` objects on every iteration, leading to $O(n^2)$ heap allocation and severe Garbage Collection pauses.
- ❌ **Violation:**
  ```java
  String csv = "";
  for (String row : rows) {
      csv += row + "\n"; // Allocates new String copy on every iteration!
  }
  ```
- ✅ **Remediation:**
  ```java
  StringBuilder csv = new StringBuilder();
  for (String row : rows) {
      csv.append(row).append("\n");
  }
  ```

---

### [AG026] Remote I/O in `@Transactional` Boundary
- **Severity:** `ERROR`
- **Rationale:** Executing remote HTTP, REST, RPC, email, or cloud SDK calls inside a `@Transactional` boundary locks the database connection from HikariCP pool idle for seconds while waiting for network responses. Under concurrent load, connection pool starvation occurs in milliseconds, crashing the entire microservice (`Connection is not available, request timed out after 30000ms`).
- ❌ **Violation:**
  ```java
  @Service
  public class OrderCheckoutService {
      @Autowired private OrderRepository orderRepository;
      @Autowired private RestClient paymentClient;

      @Transactional // Holds physical DB connection locked during external network call!
      public void checkout(OrderRequest request) {
          Order order = orderRepository.save(new Order(request));

          // Remote HTTP call takes 2-5 seconds! HikariCP connection held idle!
          PaymentResponse payment = paymentClient.post()
                  .uri("/charge")
                  .body(request)
                  .retrieve()
                  .body(PaymentResponse.class);

          order.setStatus(payment.status());
          orderRepository.save(order);
      }
  }
  ```
- ✅ **Remediation:**
  1. **Use Programmatic `TransactionTemplate`:** Keep the remote HTTP call outside the transaction boundary and wrap only the database write operations inside `transactionTemplate.executeWithoutResult(...)`.
  2. **Use the Orchestrator Pattern:** Separate concerns into a non-transactional orchestrator service (which coordinates HTTP calls) and a dedicated `@Transactional` persistence service.

---

### [AG027] Java 21 Virtual Thread Pinning
- **Severity:** `WARNING`
- **Rationale:** When running on Java 21 Virtual Threads (Project Loom), entering a `synchronized` method or `synchronized(lock)` block **pins** the virtual thread to its underlying OS carrier thread. If blocking I/O (database, socket, REST call) occurs inside the synchronized block, the carrier thread is frozen, starving the virtual thread scheduler and dropping concurrency throughput from 50,000 req/sec down to 10 req/sec.
- ❌ **Violation:**
  ```java
  @Service
  public class CurrencyExchangeService {
      private final Map<String, BigDecimal> cache = new HashMap<>();

      // Synchronized method pins OS carrier thread in Java 21!
      public synchronized BigDecimal getExchangeRate(String currencyPair) {
          if (!cache.containsKey(currencyPair)) {
              cache.put(currencyPair, fetchRateFromRemoteOrDb(currencyPair)); // Blocking I/O while pinned!
          }
          return cache.get(currencyPair);
      }
  }
  ```
- ✅ **Remediation:**
  1. Replace `synchronized` with `java.util.concurrent.locks.ReentrantLock`, which properly unmounts virtual threads without pinning carrier threads:
     ```java
     private final ReentrantLock lock = new ReentrantLock();

     public BigDecimal getExchangeRate(String currencyPair) {
         lock.lock(); // Safe: does NOT pin carrier threads
         try {
             return cache.computeIfAbsent(currencyPair, this::fetchRateFromRemoteOrDb);
         } finally {
             lock.unlock();
         }
     }
     ```
  2. Use concurrent non-blocking data structures like `ConcurrentHashMap.computeIfAbsent(...)`.

---

### [AG028] ThreadLocal Memory Leak & Context Contamination
- **Severity:** `ERROR`
- **Rationale:** Web servlet containers (Tomcat, Jetty, Undertow) use fixed worker thread pools to handle incoming HTTP requests. Calling `ThreadLocal.set()` without calling `ThreadLocal.remove()` in an enclosing `finally` block leaves values attached to the pooled `Thread` instance indefinitely. This causes:
  1. **Cross-Tenant Data Contamination / Security Breaches:** A subsequent request from User B served by the same recycled worker thread reads User A's tenant ID, user token, or security permissions.
  2. **Metaspace & Heap ClassLoader Leaks:** If custom classes or classloaders are retained in `ThreadLocal` references during dynamic application redeployment, the GC cannot collect the classloader, causing `OutOfMemoryError: Metaspace`.
- ❌ **Violation:**
  ```java
  @Service
  public class UserContextService {
      private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

      public void processRequest(SecurityContext context) {
          contextHolder.set(context); // Leaks into thread pool if an exception is thrown or method exits!
          businessLogic();
          // Even if contextHolder.remove() is here, an uncaught exception skips it!
      }
  }
  ```
- ✅ **Remediation:**
  1. Always wrap usage in a `try-finally` block ensuring `.remove()` is executed under all exit paths:
     ```java
     contextHolder.set(context);
     try {
         businessLogic();
     } finally {
         contextHolder.remove(); // Guaranteed cleanup upon return or exception
     }
     ```
  2. In Java 21+, migrate to immutable **`ScopedValue`** (JEP 446 / JEP 429), which automatically bounds value lifetime to the syntactic execution scope without thread leakage risk.

---

### [AG029] Unbounded `@Async` Thread Pool Exhaustion
- **Severity:** `WARNING`
- **Rationale:** In Spring Framework and Spring Boot, applying `@Async` without specifying a bean qualifier (e.g. `@Async("taskExecutor")`) causes Spring to search for an `Executor` bean. If no single unique `TaskExecutor` bean is present or configured, Spring defaults to `SimpleAsyncTaskExecutor`. 
  `SimpleAsyncTaskExecutor` **does not reuse threads**; it spawns a brand-new OS platform thread for every single asynchronous invocation. Under moderate-to-high traffic bursts, thousands of concurrent threads are spawned, resulting in immediate native thread exhaustion (`java.lang.OutOfMemoryError: unable to create new native thread`) and JVM crash.
- ❌ **Violation:**
  ```java
  @Service
  public class NotificationService {

      @Async // Missing executor qualifier! Falls back to unbounded SimpleAsyncTaskExecutor!
      public void sendEmailNotification(String orderId) {
          emailGateway.send(orderId);
      }
  }
  ```
- ✅ **Remediation:**
  1. Define a bounded, production-ready `ThreadPoolTaskExecutor` bean with bounded queue and rejection policy in your configuration:
     ```java
     @Configuration
     @EnableAsync
     public class AsyncConfig {
         @Bean("boundedTaskExecutor")
         public Executor boundedTaskExecutor() {
             ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
             executor.setCorePoolSize(10);
             executor.setMaxPoolSize(50);
             executor.setQueueCapacity(500);
             executor.setThreadNamePrefix("async-worker-");
             executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
             executor.initialize();
             return executor;
         }
     }
     ```
  2. Explicitly specify the qualified bean name in `@Async`:
     ```java
     @Async("boundedTaskExecutor") // Bounded execution prevents thread starvation
     public void sendEmailNotification(String orderId) {
         emailGateway.send(orderId);
     }
     ```

---

## 8. In-Code Rule Suppressions

ArchGuard supports fine-grained in-code suppressions on classes and methods when intentional exceptions must be documented in code:

### 1. Dedicated `@SuppressArchitecture` Annotation
```java
import com.architectureguard.core.annotation.SuppressArchitecture;

// Suppress specific rule
@SuppressArchitecture(rule = "AG001", reason = "Legacy adapter requires direct repo access until v2 migration")
public class LegacyOrderController {
    // ...
}

// Suppress multiple rules
@SuppressArchitecture(rules = {"AG007", "AG011"}, reason = "CLI debugging tool")
public class InternalDiagnosticService {
    // ...
}

// Suppress all rules
@SuppressArchitecture("ALL")
public class ThirdPartyGeneratedBridge {
    // ...
}
```

### 2. Standard Java `@SuppressWarnings`
```java
@SuppressWarnings("archguard:AG001")
public class LegacyPaymentGateway {
    // ...
}
```

---

## 9. Configuration File Reference (`archguard.yml`)

You can place an `archguard.yml` or `architecture-guard.json` file in your project root to customize layer package conventions, rule severities, thresholds, and exclusions:

```yaml
project:
  name: "demo-application"
  base-packages:
    - "com.example.demo"

thresholds:
  max-method-lines: 50
  max-file-lines: 300
  max-injected-dependencies: 5
  max-cyclomatic-complexity: 15
  max-cognitive-complexity: 15

layers:
  CONTROLLER:
    packages: ["..controller..", "..controllers..", "..web..", "..api.."]
    may-only-access: ["SERVICE", "MODEL", "COMMON"]
  SERVICE:
    packages: ["..service..", "..services.."]
    may-only-access: ["REPOSITORY", "MODEL", "COMMON", "SERVICE"]
  REPOSITORY:
    packages: ["..repository..", "..repositories..", "..dao.."]
    may-only-access: ["MODEL", "COMMON"]
  MODEL:
    packages: ["..model..", "..models..", "..entity..", "..domain..", "..dto.."]
    may-only-access: ["COMMON"]

reporting:
  formats: ["CONSOLE", "JSON", "HTML", "SARIF"]
  output-dir: "target/architecture-guard"
  fail-on: "ERROR"
  fail-on-new-violations: true
```

---

## 10. Reports, Diagnostics & Automated PR Code Review

ArchGuard automatically generates comprehensive reporting artifacts in `target/architecture-guard/` during `mvn verify` / `mvn architecture-guard:check`:

1. **`report.sarif` (Standard OASIS SARIF 2.1.0):**
   - Industry-standard Static Analysis Results Interchange Format.
   - Natively supported by **GitHub Code Scanning**, **GitLab Code Quality**, and **Azure DevOps PR annotations** to highlight violations directly on pull request lines of code.

2. **`code-review.md` (Automated Pull Request Code Review):**
   - **Focused on Delta:** Evaluates **only the newly added violations** against `.architecture-guard-baseline.json`. Legacy issues are filtered out.
   - **Automated Verdict:** Generates clear verdict headers (`APPROVED`, `APPROVED WITH COMMENTS`, or `CHANGES REQUESTED`).
   - **File-by-File Review Comments:** Provides exact line numbers, severity emojis (🔴/🟡), issue explanations, and copy-pasteable **Before vs. After remediation snippets**.
   - **Reviewer Checklist:** Auto-generates a clean markdown checklist for peer reviewers and tech leads.

3. **`report.html` (Offline Interactive Dashboard):**
   - Modern, responsive dark-themed dashboard with zero external CDN dependencies.
   - **PR Code Review Banner:** Shows review verdict and 1-click filter buttons (`Focus on PR Delta`, `Show Errors Only`).
   - Live search and filter bar for rules, classes, and severities.
   - Distinct badges (`NEW` vs `BASELINED`).
   - Monospace formatted code evidence blocks with exact line numbers and remediation tips.

4. **`report.json` (Machine-Readable Output):**
   - Full structured schema containing execution metadata, rule results, SHA-256 fingerprints, and differential breakdowns.

5. **Console Output:**
   - Colorized ANSI console summary during Maven builds.

---

## 11. CI/CD Pipeline Integration

### GitHub Actions Workflow Example with SARIF PR Inline Annotations (`.github/workflows/architecture-check.yml`)

```yaml
name: Architecture Quality Gate

on:
  push:
    branches: [ main, master, develop ]
  pull_request:
    branches: [ main, master ]

jobs:
  archguard:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run Architecture Quality Gate
        run: mvn clean verify -Darchguard.failOnSeverity=ERROR

      - name: Upload SARIF to GitHub Code Scanning (PR Inline Annotations)
        if: always()
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: target/architecture-guard/report.sarif
          category: archguard

      - name: Upload Architecture Report Artifacts
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: architecture-guard-report
          path: target/architecture-guard/
```

---

## 12. Troubleshooting & FAQ

#### Q1: Why did the build succeed even though a new violation appeared in the report?
**Answer:** ArchGuard defaults to `<failOnSeverity>ERROR</failOnSeverity>`. If your new violation is a `WARNING` (e.g. `System.out.println` or `@Autowired`), it will be marked as `NEW` in the report but will not break the build. To fail on warnings, set `<failOnSeverity>WARNING</failOnSeverity>` in your `pom.xml` or pass `-Darchguard.failOnSeverity=WARNING`.

#### Q2: What happens when I rename or move a class?
**Answer:** ArchGuard’s fingerprinting uses the fully qualified class name. If a class is renamed, any legacy baseline violations in the old class will be marked as `RESOLVED`, and violations in the new class name will be tracked as `NEW`. You can re-run `mvn architecture-guard:baseline` to update the snapshot.

#### Q3: How do I exclude generated classes (MapStruct, Lombok, OpenAPI)?
**Answer:** In `archguard.yml` / `architecture-guard.json`, add regex patterns to the `"exclusions"` list (e.g., `[".*Generated.*", ".*MapperImpl.*"]`), or use in-code `@SuppressArchitecture("ALL")`.

---
*Maintained by the Architecture Guard Core Team. Built for Java 21 & Spring Boot.*
