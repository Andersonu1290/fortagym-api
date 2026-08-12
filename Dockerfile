# Paso 1: Construcción con una imagen de Maven moderna
FROM maven:3.8.7-eclipse-temurin-17 AS build
COPY . .
# Construimos el proyecto saltando los tests para ir más rápido
RUN mvn clean package -DskipTests

# Paso 2: Ejecución con la imagen estable de Eclipse Temurin
FROM eclipse-temurin:17-jdk-jammy
# IMPORTANTE: Verifica que el nombre del .jar sea fortagym-0.0.1-SNAPSHOT.jar
COPY --from=build /target/fortagym-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto por defecto (opcional, Render lo ignora y usa el suyo)
EXPOSE 8080

# 🔥 LA MAGIA AQUÍ: Limitamos la RAM a 256MB máximo y activamos el perfil prod
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
