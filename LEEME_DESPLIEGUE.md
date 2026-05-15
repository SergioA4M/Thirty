# INSTRUCCIONES DE DESPLIEGUE Y EJECUCIÓN (PROYECTO THIRTY)

## 1. REQUISITOS PREVIOS
- **Java Development Kit (JDK) 17**.
- **Maven** (Opcional, se incluye Maven Wrapper `mvnw` en el proyecto).
- **PostgreSQL** (Local) o conexión a internet para usar la base de datos de Supabase configurada.

## 2. VARIABLES DE ENTORNO REQUERIDAS
Por motivos de seguridad, las contraseñas no se incluyen en el código fuente. Antes de ejecutar, configurar las siguientes variables de entorno en el sistema o en el IDE (Run Configurations):
- `DB_PASSWORD` = [Contraseña de la BD de Supabase]
- `SUPABASE_KEY` = [API Key de Supabase]

## 3. BASE DE DATOS
- El proyecto utiliza Hibernate con la configuración `spring.jpa.hibernate.ddl-auto=update`.
- Al ejecutar el servidor, las tablas se crearán y estructurarán automáticamente en PostgreSQL.
- Como referencia académica, se adjunta el archivo `schema.sql` con la estructura física de la base de datos.

## 4. EJECUCIÓN DEL SERVIDOR (BACKEND)
- Abrir una terminal en la raíz del proyecto.
- Ejecutar el siguiente comando dependiendo de su sistema operativo:
  - **Windows**:
    ```powershell
    .\mvnw.cmd spring-boot:run
    ```
  - **Mac/Linux**:
    ```bash
    ./mvnw spring-boot:run
    ```

## 5. ACCESO AL CLIENTE (FRONTEND)
- Una vez el servidor esté iniciado (*Tomcat started on port 8080*), abrir un navegador web.
- Acceder a: [http://localhost:8080/index.html](http://localhost:8080/index.html)