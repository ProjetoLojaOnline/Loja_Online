# Etapa 1: Build da aplicação
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia o pom.xml e baixa dependências primeiro (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Compila o projeto sem rodar testes
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final e leve para execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia o JAR gerado na etapa anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]