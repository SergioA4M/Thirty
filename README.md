<div align="center">
  <h1>👥 Thirty</h1>
  <p><strong>Una red social nostálgica inspirada en la esencia clásica de Tuenti.</strong></p>

  <div>
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
    <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML5" />
    <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white" alt="CSS3" />
    <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" alt="JavaScript" />
  </div>
</div>

<br />

**Thirty** es un proyecto de desarrollo web creado como **Trabajo de Fin de Grado (TFG)**. Su objetivo es emular la experiencia de las redes sociales clásicas, centrándose en la comunicación directa con amigos, compartiendo momentos y descubriendo novedades.

---

## ✨ Características Principales

*   📝 **Tablón de Novedades (Muro):** Publica estados, fotos, comparte vídeos de YouTube y reacciona a lo que publican tus amigos.
*   📸 **Stories:** Sube fotos temporales que tus amigos pueden ver y comentar.
*   💬 **Chat Interactivo:** Mensajería privada en tiempo real con soporte para envío de imágenes, emojis y stickers.
*   🔔 **Notificaciones Centralizadas:** Avisos inmediatos sobre nuevos likes, comentarios, mensajes y solicitudes de amistad.
*   👥 **Sistema de Amistad:** Busca a otras personas, envíales peticiones y mira quién está conectado gracias al indicador de estado *Online*.
*   👤 **Perfiles Personalizables:** Edita tu biografía, sube tu avatar sin límite de tamaño y crea un "Espacio Personal" incrustando listas de Spotify o contenido externo.
*   🔗 **Compartir (Open Graph):** Comparte fácilmente las publicaciones en X (Twitter), Facebook, LinkedIn y WhatsApp con tarjetas de vista previa dinámicas.

## 🛠️ Arquitectura y Tecnologías

*   **Backend:** Construido en Java 17 utilizando **Spring Boot** (Spring Web, Spring Data JPA). Funciona como una API RESTful.
*   **Base de Datos:** **PostgreSQL** alojado en Supabase para almacenamiento persistente y robusto de datos y relaciones complejas (como la tabla Many-to-Many de amigos).
*   **Frontend:** Interfaz limpia construida puramente con **HTML, CSS y Vanilla JavaScript**. Comunicación fluida con el backend vía *Fetch API*.
*   **Despliegue:** API backend alojada en la plataforma **Render.com**.

## 🚀 Instalación y Uso en Local

Si deseas clonar el proyecto y ejecutarlo en tu máquina local, sigue estos pasos:

1. **Clona el repositorio:**
   ```bash
   git clone https://github.com/SergioA4M/Thirty.git
   cd Thirty
   ```

2. **Configura la Base de Datos:**
   Abre `src/main/resources/application.properties` y configura las credenciales de tu servidor PostgreSQL:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/tu_base_de_datos
   spring.datasource.username=tu_usuario
   spring.datasource.password=tu_contrasena
   ```

3. **Inicia la Aplicación:**
   Usa el Wrapper de Maven incluido para compilar y lanzar el backend:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Si estás en Windows usa `mvnw.cmd spring-boot:run`)*

4. **Abre la App:**
   Navega a `http://localhost:8080/login.html` desde tu navegador.

## 📸 Capturas de Pantalla

> **Tip para Sergio:** *Toma unas cuantas capturas de pantalla de la página de Login, tu Muro principal, el Chat y tu Perfil, guárdalas en una carpeta `/assets` o `/docs` en tu repositorio y enlázalas aquí para que el proyecto luzca increíble a simple vista.*

<div align="center">
  <br/>
  <i>Desarrollado con ☕ por Sergio para su TFG</i>
</div>
