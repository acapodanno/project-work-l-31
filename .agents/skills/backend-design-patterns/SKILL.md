---
name: backend-design-patterns
description: Guidelines and instructions for applying backend design patterns (like Strategy) in Spring Boot. Trigger when building or refactoring complex business logic, calculation engines, or multi-variant algorithms in Java.
---

# Backend Design Patterns (Spring Boot)

When working on the Java backend for this project, you should actively look for opportunities to apply modern design patterns, particularly the **Strategy Pattern**, to keep the code clean, testable, and compliant with SOLID principles.

## 1. The Strategy Pattern in Spring Boot
Use the Strategy pattern when you have multiple algorithms or behaviors for a specific task (e.g., handling different types of Medical Reports, processing different types of Appointments, or calculating stats).

### How to Implement:
1. **Define the Interface:** Create an interface for the strategy.
```java
public interface ReportProcessorStrategy {
    void process(MedicalReport report);
    ReportType getType();
}
```

2. **Implement Strategies:** Create Spring Components (`@Component`) that implement the interface.
```java
@Component
public class BloodTestProcessorStrategy implements ReportProcessorStrategy {
    @Override
    public void process(MedicalReport report) { /* logic */ }
    @Override
    public ReportType getType() { return ReportType.BLOOD_TEST; }
}
```

3. **Create the Factory/Context:** Create a component that injects `List<ReportProcessorStrategy>` and maps them for O(1) lookup.
```java
@Component
public class ReportProcessorFactory {
    private final Map<ReportType, ReportProcessorStrategy> strategies;

    public ReportProcessorFactory(List<ReportProcessorStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(ReportProcessorStrategy::getType, Function.identity()));
    }

    public ReportProcessorStrategy getStrategy(ReportType type) {
        return strategies.get(type);
    }
}
```

## 2. SOLID Principles in Practice
Always validate your code changes against the five SOLID principles:

1. **S - Single Responsibility Principle (SRP):**
   - A class should have one, and only one, reason to change.
   - Example: Don't mix data access, business logic, and presentation in a single class. A `ReportGenerator` shouldn't also be responsible for emailing the report.
2. **O - Open/Closed Principle (OCP):**
   - Software entities should be open for extension, but closed for modification.
   - Example: If a new report type is added, you shouldn't have to modify the core `ReportService` class. Instead, implement a new `ReportProcessorStrategy` (as shown above).
3. **L - Liskov Substitution Principle (LSP):**
   - Objects of a superclass shall be replaceable with objects of its subclasses without breaking the application.
   - Example: Ensure interfaces are lean enough that implementors don't throw `UnsupportedOperationException` for methods they don't logically support.
4. **I - Interface Segregation Principle (ISP):**
   - No client should be forced to depend on methods it does not use.
   - Example: Instead of a massive `HealthcareService` interface, split it into `AppointmentService`, `DoctorService`, `PatientService`.
5. **D - Dependency Inversion Principle (DIP):**
   - High-level modules should not depend on low-level modules. Both should depend on abstractions.
   - Example: A Controller should depend on an interface (e.g., `PaymentService`), not a specific implementation (e.g., `StripePaymentServiceImpl`).

## 3. General Best Practices
- **Favor Composition over Inheritance.**
- **Keep Controllers thin:** Controllers should only handle HTTP routing and DTO mapping. All business logic goes to Services or Strategy objects.
- **Dependency Injection:** Use Constructor injection via Lombok's `@RequiredArgsConstructor`.

## 4. DTOs and Data Transfer
- **Use Records for DTOs:** Always use Java 14+ `record` for Data Transfer Objects (DTOs) instead of standard classes with Lombok's `@Data`. Records provide built-in immutability, `equals`, `hashCode`, and `toString` methods natively.
- **Validation & Transformation:** If you need to validate fields upon creation or transform them (e.g., standardizing strings, throwing exceptions for invalid states to ensure strict immutability), use **compact constructors**.

Example:
```java
public record PatientDTO(Long id, String name, String email) {
    // Compact constructor for validation/transformation
    public PatientDTO {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        email = email != null ? email.toLowerCase() : null; // Transformation
    }
}
```
