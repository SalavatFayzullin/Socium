# Многоэтапная сборка для оптимизации размера образа
FROM eclipse-temurin:17-jdk-alpine AS builder

# Установка рабочей директории
WORKDIR /app

# Копирование Maven Wrapper и pom.xml
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Загрузка зависимостей
RUN chmod +x mvnw && ./mvnw dependency:resolve

# Копирование исходного кода
COPY src ./src

# Сборка приложения
RUN ./mvnw clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:17-jre-alpine

# Установка рабочей директории
WORKDIR /app

# Установка curl для health check и создание пользователя
RUN apk add --no-cache curl && \
    addgroup -g 1001 -S socium && \
    adduser -u 1001 -S socium -G socium

# Копирование JAR файла из builder этапа
COPY --from=builder /app/target/*.jar app.jar

# Создание директорий для данных и загрузок
RUN mkdir -p /app/data /app/uploads && \
    chown -R socium:socium /app

# Переключение на непривилегированного пользователя
USER socium

# Настройка портов
EXPOSE 8080

# Настройка health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 