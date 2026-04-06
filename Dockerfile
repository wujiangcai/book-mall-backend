FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml

RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
COPY config/README.md config/README.md

RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_CONFIG_DIR=/app/config

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/target/book-mall-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8003

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
