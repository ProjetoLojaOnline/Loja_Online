# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.16-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Baixa dependências primeiro para aproveitar cache de camada
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuário não-root: evita execução como root no container
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# --chown evita um RUN chown separado (menos camadas)
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

# Requer spring-boot-starter-actuator no pom.xml
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
