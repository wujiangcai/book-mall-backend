# Phase 8 Alignment

## Context

Phase 8 focuses on non-functional quality: consistent validation, safe logging with sensitive-data masking, SQL/query safety, pagination caps, and security/CORS guardrails. The codebase already uses DTO validation and centralized exception handling, but enforcement, logging hygiene, and pagination behavior need to be standardized across endpoints. User confirmed Phase 8 scope: validation consistency, pagination caps, logging hygiene, SQL/soft-delete checks, and security/CORS guardrails.

## Recommended Approach

### 1) Validation consistency

- Audit request DTOs to ensure required fields are annotated (`@NotNull`, `@NotBlank`, `@Min`, `@DecimalMin`, `@Pattern`).
- Ensure controllers using `@RequestBody` apply `@Valid` (already in place).
- Apply `@Validated` at controller level where query param constraints are used; add `@Min` to pagination and ID query params.
- Keep validation errors unified via `GlobalExceptionHandler`.

### 2) Pagination defaults and caps

- Ensure all list endpoints normalize `page/pageSize` and cap size at 100 (reuse existing normalization in services like `BookServiceImpl`, `OrderServiceImpl`, `UserServiceImpl`).
- Align defaults across services (default 20, max 100).
- Avoid unbounded queries by requiring page/pageSize or normalizing nulls.

### 3) Logging hygiene with masking

- Add masking for sensitive fields in error logs (token/password) inside centralized logging paths.
- Keep business exception responses unchanged while avoiding leaking secrets in logs.
- Review logging levels in `application.properties` and ensure mapper SQL logging is suitable for non-dev use.

### 4) SQL practices and soft-delete filters

- Verify all MyBatis queries use parameterized inputs and avoid string concatenation.
- Ensure soft-deleted records are filtered consistently (`status <> -1`) across read paths.
- Confirm list/count queries share identical filters to keep pagination totals accurate.

### 5) Security and CORS guardrails

- Verify interceptor coverage for `/api/front/**` and `/api/admin/**` and minimize exclusions.
- Ensure admin-only access is enforced via `AdminAuthInterceptor` and role checks.
- Keep CORS origins configurable and avoid wildcard origins in production.

## Files to Modify

### Validation & Pagination

- src/main/java/top/wjc/bookmallbackend/controller/front/FrontBookController.java
- src/main/java/top/wjc/bookmallbackend/controller/admin/AdminBookController.java
- src/main/java/top/wjc/bookmallbackend/controller/front/FrontOrderController.java
- src/main/java/top/wjc/bookmallbackend/controller/admin/AdminOrderController.java
- src/main/java/top/wjc/bookmallbackend/controller/admin/AdminUserController.java
- src/main/java/top/wjc/bookmallbackend/service/impl/BookServiceImpl.java
- src/main/java/top/wjc/bookmallbackend/service/impl/OrderServiceImpl.java
- src/main/java/top/wjc/bookmallbackend/service/impl/UserServiceImpl.java

### Logging

- src/main/java/top/wjc/bookmallbackend/exception/GlobalExceptionHandler.java
- src/main/resources/application.properties

### SQL/Soft-delete

- src/main/resources/mapper/BookMapper.xml
- src/main/resources/mapper/CategoryMapper.xml
- src/main/resources/mapper/BannerMapper.xml
- src/main/resources/mapper/CartMapper.xml
- src/main/resources/mapper/OrderMapper.xml
- src/main/resources/mapper/UserMapper.xml

### Security/CORS

- src/main/java/top/wjc/bookmallbackend/config/WebMvcConfig.java
- src/main/java/top/wjc/bookmallbackend/config/CorsConfig.java
- src/main/java/top/wjc/bookmallbackend/config/SecurityConfig.java

## Verification

- Validation:
  - Invalid inputs return 400 with consistent message format.
  - Query param constraints (page/pageSize/userId/categoryId) reject invalid values.
- Pagination:
  - pageSize defaults to 20 and caps at 100.
- Logging:
  - Logs do not contain token/password values.
  - MyBatis SQL logging is disabled or reduced for non-dev.
- SQL/soft-delete:
  - Soft-deleted records are excluded from list/detail APIs.
  - List/count filters match for correct totals.
- Security/CORS:
  - Interceptor coverage unchanged for public endpoints.
  - CORS origins remain configurable and non-wildcard.
