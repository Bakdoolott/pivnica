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

# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]