# ======================== СТАДИЯ СБОРКИ ========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кешируем слой)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# ======================== СТАДИЯ ЗАПУСКА ========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR из стадии сборки
COPY --from=build /app/target/gateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]