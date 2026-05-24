# Paso 1: Construcción con una imagen de Maven moderna
FROM maven:3.8.7-eclipse-temurin-17 AS build
COPY . .
# Construimos el proyecto saltando los tests para ir más rápido
RUN mvn clean package -DskipTests

# Paso 2: Ejecución con la imagen estable de Eclipse Temurin
FROM eclipse-temurin:17-jdk-jammy
# IMPORTANTE: Verifica que el nombre del .jar sea fortagym-0.0.1-SNAPSHOT.jar
COPY --from=build /target/fortagym-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto para la nube (8080)
EXPOSE 8080

# 🔥 LA MAGIA AQUÍ: Le decimos a Java que active el perfil "prod" para leer los enlaces web
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
