package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Comentario;
import thirty_api.models.User;
import thirty_api.models.Notificacion;
import thirty_api.repositories.ComentarioRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.NotificacionRepository;

import java.util.List;

/**
 * Controlador de Comentarios (ComentarioController).
 *
 * ¿Qué es esto?
 * En Spring Boot, un @RestController es una clase que maneja las peticiones que
 * llegan desde el navegador o la app (Frontend) a través de Internet (URLs).
 * Este controlador se encarga de todo lo relacionado con los comentarios que los usuarios
 * se dejan en sus perfiles (el famoso "Muro" de Tuenti).
 */
@RestController // Indica que esta clase responderá con datos (normalmente JSON), no con páginas HTML directamente.
@RequestMapping("/api/comentarios") // Todas las URLs de este controlador empezarán por "/api/comentarios"
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite que cualquier página web pueda pedirle datos a esta API (Cross-Origin Resource Sharing)
public class ComentarioController {

    // Inyectamos (@Autowired) los repositorios necesarios. 
    // Los repositorios son las interfaces que se comunican directamente con la base de datos (PostgreSQL).
    @Autowired private ComentarioRepository comentarioRepository; // Para guardar, leer y borrar comentarios.
    @Autowired private UserRepository userRepository;             // Para buscar información de los usuarios.
    @Autowired private NotificacionRepository notificacionRepository; // Para enviar notificaciones cuando hay un comentario nuevo.

    /**
     * Endpoint para publicar un nuevo comentario en el perfil de alguien.
     * @PostMapping significa que esta función se ejecuta cuando el Frontend envía datos mediante el método POST (para crear).
     * URL: POST /api/comentarios/{perfilId}/{autorId}
     *   - perfilId: El ID del usuario que recibe el comentario (dueño del muro).
     *   - autorId: El ID del usuario que está escribiendo el comentario.
     */
    @PostMapping("/{perfilId}/{autorId}")
    public Comentario publicar(@PathVariable Long perfilId, @PathVariable Long autorId, @RequestBody String texto) {
        // 1. Buscamos en la base de datos a los dos usuarios implicados usando los IDs de la URL.
        User perfil = userRepository.findById(perfilId).get(); // El dueño del muro
        User autor = userRepository.findById(autorId).get();   // Quien escribe el comentario

        // 2. Creamos un nuevo objeto Comentario y le asignamos sus datos.
        Comentario c = new Comentario();
        c.setContenido(texto);   // El texto del comentario (que viene en el "body" de la petición HTTP)
        c.setPerfil(perfil);     // Quién recibe el comentario
        c.setAutor(autor);       // Quién lo escribe
        c.setLeido(false);       // Por defecto, cuando se crea un comentario, no ha sido leído aún.
        
        // 3. Guardamos el comentario en la base de datos.
        Comentario guardado = comentarioRepository.save(c);

        // 4. Lógica de notificaciones:
        // Si el usuario no se está comentando a sí mismo, le enviamos una notificación al dueño del muro.
        if (!perfilId.equals(autorId)) {
            Notificacion notif = new Notificacion();
            notif.setUsuario(perfil); // Quien recibe la notificación
            notif.setEmisor(autor);   // Quien provocó la notificación
            notif.setTipo("comentario"); // Categoría de la notificación para mostrar el icono correcto en el frontend
            notif.setContenido(autor.getFirstName() + " comentó en tu perfil"); // Mensaje a mostrar
            notif.setEntidadId(guardado.getId()); // Guardamos el ID del comentario al que hace referencia
            
            // Guardamos la notificación en la base de datos
            notificacionRepository.save(notif);
        }

        // Devolvemos el comentario recién guardado al Frontend (con su fecha generada, ID, etc.).
        return guardado;
    }

    /**
     * Endpoint para obtener todos los comentarios del muro de un usuario.
     * @GetMapping responde a peticiones GET (para leer o pedir datos).
     * URL: GET /api/comentarios/perfil/{perfilId}
     */
    @GetMapping("/perfil/{perfilId}")
    public List<Comentario> getComentarios(@PathVariable Long perfilId) {
        // 1. Pedimos a la base de datos todos los comentarios del usuario, ordenados desde el más nuevo al más antiguo (Descendente).
        List<Comentario> lista = comentarioRepository.findByPerfilIdOrderByFechaDesc(perfilId);
        
        // 2. Como el dueño del perfil está viendo su muro, marcamos todos los comentarios como "leídos".
        lista.forEach(com -> {
            if (!com.isLeido()) { // Si no estaba leído...
                com.setLeido(true); // Lo marcamos como leído
                comentarioRepository.save(com); // Actualizamos ese comentario en la base de datos
            }
        });
        
        // 3. Devolvemos la lista de comentarios al Frontend para que los pinte en la pantalla.
        return lista;
    }

    /**
     * Endpoint para eliminar un comentario.
     * @DeleteMapping responde a peticiones DELETE.
     * URL: DELETE /api/comentarios/{id}
     */
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        // Le decimos a la base de datos que borre el comentario con el ID proporcionado.
        comentarioRepository.deleteById(id);
    }
}
