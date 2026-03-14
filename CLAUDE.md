# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common commands
- Run app (Spring Boot): `mvn spring-boot:run`
- Run tests: `mvn test`
- Package: `mvn -DskipTests package`

## Architecture overview
- Spring Boot app entry: `src/main/java/top/wjc/bookmallbackend/BookMallBackendApplication.java`
- Web/API layer: controllers under `src/main/java/top/wjc/bookmallbackend/controller/`
  - Front auth: `/api/front/auth/*`
  - Admin auth: `/api/admin/auth/*`
  - User profile: `/api/front/user/*`
- Auth/security:
  - JWT helper in `util/JwtUtil.java`
  - Request interceptors in `interceptor/` enforce JWT for `/api/front/**` and admin-only for `/api/admin/**` (configured in `config/WebMvcConfig.java`).
  - Spring Security is permissive; access control is primarily via interceptors (`config/SecurityConfig.java`).
- Persistence: MyBatis
  - Mappers in `src/main/java/top/wjc/bookmallbackend/mapper/`
  - XML mappings in `src/main/resources/mapper/`
  - MyBatis config in `config/MyBatisConfig.java`
- Data model: SQL schema in `src/main/resources/sql/schema.sql` with tables for user, category, book, address, cart, order, order_item, banner.
- Config: `src/main/resources/application.properties` (DB, JWT, CORS, logging, Swagger paths)
