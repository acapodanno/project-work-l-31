---
name: enforce-unit-tests
description: Automatically enforces the creation of unit tests for any new feature, class, or functionality added to the project.
---

# Enforce Unit Tests

## Trigger
Trigger this skill whenever you write new code, add a new feature, or refactor an existing class in any of the projects within this repository.

## Instructions
1. Every time you write new code (e.g., Services, Controllers, Mappers, Utilities), you MUST also write the corresponding unit tests to maintain 100% test coverage.
2. The only exceptions to this rule are DTOs and Entities, which do not require unit tests as they are data structures without business logic.
3. The tests must verify both positive and negative scenarios, including exceptions and edge cases.
4. Ensure the tests are placed in the appropriate `src/test/java` or corresponding test directory for other languages (like Frontend or Agent).
5. Run the tests using the appropriate tool (e.g., `mvn test` for Java, `npm test` for Frontend) and verify that they pass before completing the task.
