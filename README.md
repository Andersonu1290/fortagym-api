# FortaGym - Backend REST API 🏋️‍♂️⚙️

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

FortaGym API es el núcleo (backend) de la plataforma de gestión de gimnasios FortaGym. Construida con una arquitectura orientada a microservicios monolíticos utilizando **Java 17** y **Spring Boot 3**, esta API RESTful segura y escalable maneja toda la lógica de negocio, procesamiento de pagos, control de inventario, reservas y gestión de usuarios.

## 🚀 Módulos y Características Principales

*   **Seguridad y Autenticación:** 
    *   Implementación de **JSON Web Tokens (JWT)** para el control de sesiones stateless.
    *   Control de acceso basado en roles (RBAC): `ADMIN`, `ENTRENADOR`, `NUTRICIONISTA` y `USUARIO`.
    *   Filtros personalizados y encriptación de contraseñas con BCrypt.
*   **Gestión de Usuarios y Perfiles:** 
    *   Registro, validación de datos únicos (Email, DNI) y subida de avatares mediante `MultipartFile`.
*   **E-commerce y Carrito de Compras:** 
    *   Catálogo de productos, control de stock en tiempo real y lógica transaccional (`@Transactional`) para evitar "cobros fantasma".
    *   Cálculo automático de IGV, descuentos por cupones y costos de envío.
*   **Reservas y Calendario:** 
    *   Sistema inteligente de agendamiento para entrenadores y nutricionistas.
    *   Validación de límites de reservas mensuales según el tipo de membresía activa.
    *   Tareas programadas (`@Scheduled`) para la limpieza automática de reservas vencidas.
*   **Cartilla Digital (Entrenamiento y Nutrición):** 
    *   Creación y asignación de rutinas detalladas (ejercicios, series, descansos).
    *   Evaluaciones corporales y planes nutricionales.
    *   Exportación dinámica de cartillas a formato **Excel (.xlsx)** usando Apache POI.
*   **Panel Administrativo (Dashboard):** 
    *   Consultas JPQL/SQL nativas avanzadas para la extracción de KPIs (ingresos, ocupación, ticket promedio).
    *   Generación de datos estructurados para gráficas de donas y barras en el frontend.

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Java 17
*   **Framework Core:** Spring Boot (3.5.14)
*   **Seguridad:** Spring Security + jjwt (JSON Web Token)
*   **Persistencia:** Spring Data JPA + Hibernate
*   **Base de Datos:** MySQL (Aiven Cloud para producción, Local para desarrollo)
*   **Documentación:** Springdoc OpenAPI (Swagger 3)
*   **Archivos / Reportes:** Apache POI (Excel)
*   **Despliegue:** Docker + Render

## ⚙️ Instalación y Configuración Local

### 1. Prerrequisitos
*   [Java JDK 17](https://adoptium.net/) instalado.
*   [Maven](https://maven.apache.org/) (o usar el wrapper incluido).
*   Servidor MySQL (XAMPP, Workbench o contenedor Docker).

### 2. Clonar el repositorio
```bash
git clone [https://github.com/Andersonu1290/fortagym-api.git](https://github.com/Andersonu1290/fortagym-api.git)
cd fortagym-api

3. Configurar la Base de Datos
Crea una base de datos en MySQL llamada fortagym. Luego, actualiza tus credenciales en el archivo src/main/resources/application.properties o crea un perfil application-local.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/fortagym?useSSL=false&serverTimezone=UTC
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
# Actualiza esquemas automáticamente en desarrollo
spring.jpa.hibernate.ddl-auto=update 
# Clave JWT
jwt.secret=Tu_Clave_Secreta_Larga_De_Al_Menos_256_Bits

4. Compilar y Ejecutar
Puedes ejecutar la aplicación directamente usando Maven:
mvn clean install -DskipTests
mvn spring-boot:run

La API estará disponible por defecto en http://localhost:8089.
📚 Documentación de la API (Swagger)
La API está completamente documentada y es testeable a través de Swagger UI.
Una vez que la aplicación esté corriendo, accede a:
 * Interfaz Gráfica: http://localhost:8089/swagger-ui.html
 * Especificación JSON: http://localhost:8089/v3/api-docs
Nota: Asegúrate de autenticarte haciendo clic en el botón "Authorize" de Swagger e ingresando un token JWT válido generado desde el endpoint /api/auth/login.
📦 Despliegue (Docker / Render)
El proyecto incluye un Dockerfile optimizado en dos etapas (Multistage Build) para compilar con Maven y ejecutar con una imagen ligera de Eclipse Temurin, limitando estratégicamente la memoria (-Xmx256m) para entornos gratuitos como Render.
# Construir imagen Docker
docker build -t fortagym-api .
# Ejecutar contenedor
docker run -p 8080:8080 -e PORT=8080 fortagym-api

🔗 Aplicación Frontend (Complemento)
Este repositorio contiene exclusivamente la API (Backend). La interfaz de usuario ha sido desarrollada como una SPA moderna utilizando Angular.
Puedes encontrar el código fuente del frontend, el diseño de la tienda y los paneles interactivos en el siguiente repositorio:
👉 FortaGym Front - Repositorio Frontend Web

