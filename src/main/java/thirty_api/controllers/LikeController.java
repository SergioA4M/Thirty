package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Like;
import thirty_api.models.User;
import thirty_api.models.Notificacion;
import thirty_api.models.Post;
import thirty_api.repositories.LikeRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.PostRepository;
import thirty_api.repositories.NotificacionRepository;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Controlador de Likes (Me Gusta) - LikeController.
 *
 * ¿Qué es esto?
 * En Spring Boot, un @RestController recibe las peticiones HTTP del navegador.
 * Este controlador se encarga exclusivamente de la lógica de los "Me Gusta".
 * Permite a los usuarios dar "Like" a publicaciones (posts), fotos, comentarios, etc.,
 * quitar el like (unlike), saber cuántos likes tiene algo y notificar al autor.
 */
@RestController // Indica que los métodos devuelven datos puros (generalmente en formato JSON) para que el Frontend los consuma.
@RequestMapping("/api/likes") // Todas las rutas definidas en este controlador empezarán por "/api/likes"
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite que la app web pueda conectarse sin problemas de CORS
public class LikeController {

    // @Autowired le pide a Spring Boot que inyecte (cree y nos preste) las conexiones a la base de datos (Repositories)
    @Autowired private LikeRepository likeRepository;       // Para manejar los 'Likes' en la base de datos
    @Autowired private UserRepository userRepository;       // Para buscar a los usuarios que dan el like
    @Autowired private PostRepository postRepository;       // Para interactuar con las publicaciones
    @Autowired private NotificacionRepository notificacionRepository; // Para enviar la campanita de "A X le gustó tu post"

    /**
     * Endpoint principal para dar o quitar un Like ("Toggle").
     * Si el usuario no había dado like, se lo pone. Si ya le había dado like, se lo quita.
     * @PostMapping porque estamos enviando una acción que modifica el estado del servidor.
     * URL: POST /api/likes/toggle
     */
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(@RequestBody Map<String, Object> payload) {
        // 1. Extraemos los datos que nos envía el frontend en el cuerpo (Body) de la petición.
        Long usuarioId = ((Number) payload.get("usuarioId")).longValue(); // Quién da el like
        Long entidadId = ((Number) payload.get("entidadId")).longValue(); // A qué se le da like (ID del post, ID de la foto, etc)
        String tipo = (String) payload.get("tipo");                       // Qué tipo de cosa es ("post", "foto", "comentario")
        
        // receptorId es opcional. Es el ID del dueño de la publicación (para enviarle la notificación)
        Long receptorId = payload.containsKey("receptorId") ? ((Number) payload.get("receptorId")).longValue() : null;

        // 2. Buscamos en la base de datos si ESTE usuario ya le dio like a ESTA entidad de ESTE tipo
        Optional<Like> likeExistente = likeRepository
            .findByUsuarioIdAndTipoAndEntidadId(usuarioId, tipo, entidadId);

        boolean liked; // Variable para saber si al final quedó "likeado" o no

        if (likeExistente.isPresent()) {
            // 3A. Si el like YA EXISTÍA, el usuario hizo clic para quitarlo (Unlike).
            likeRepository.delete(likeExistente.get()); // Borramos el like de la base de datos
            liked = false; // El estado final es "sin like"
        } else {
            // 3B. Si el like NO EXISTÍA, el usuario quiere dar Like.
            User usuario = userRepository.findById(usuarioId).orElse(null); // Buscamos al usuario que está dando el like
            if (usuario == null) {
                // Si el usuario no existe (error raro, sesión caducada, etc), devolvemos error
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.badRequest().body(error);
            }

            // Creamos el nuevo Like y lo rellenamos con la información
            Like nuevoLike = new Like();
            nuevoLike.setUsuario(usuario);
            nuevoLike.setTipo(tipo);
            nuevoLike.setEntidadId(entidadId);
            likeRepository.save(nuevoLike); // Lo guardamos en PostgreSQL
            liked = true; // El estado final es "con like"

            // 4. Lógica de NOTIFICACIONES. 
            // Si el like fue a un "post", tenemos el ID del receptor, y el que da el like NO es el mismo dueño del post (no te notificas a ti mismo)
            if ("post".equals(tipo) && receptorId != null && !receptorId.equals(usuarioId)) {
                User receptor = userRepository.findById(receptorId).orElse(null);
                if (receptor != null) {
                    // Contamos cuántos likes totales tiene el post ahora
                    long totalLikes = likeRepository.countByTipoAndEntidadId("post", entidadId);
                    
                    // Preparamos el mensaje para la notificación (plural o singular)
                    String contenido = totalLikes == 1 ? 
                        usuario.getFirstName() + " le dio like a tu publicación" :
                        usuario.getFirstName() + " y " + (totalLikes - 1) + " más le dieron like a tu publicación";
                    
                    // Creamos y guardamos la notificación para el dueño del post
                    Notificacion notif = new Notificacion();
                    notif.setUsuario(receptor); // Quien recibe el aviso
                    notif.setEmisor(usuario);   // Quien provocó el aviso
                    notif.setTipo("like");      // Tipo de aviso (para poner un icono de corazón, por ejemplo)
                    notif.setContenido(contenido);// El texto que calculamos arriba
                    notif.setEntidadId(entidadId);// Para que al clicar en la notificación, lo lleve al post correcto
                    notificacionRepository.save(notif);
                }
            }
        }

        // 5. Contamos el número total de likes que quedaron (después de añadirlo o quitarlo)
        long count = likeRepository.countByTipoAndEntidadId(tipo, entidadId);

        // 6. Preparamos un objeto JSON con la respuesta para el Frontend
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("liked", liked); // Booleano: ¿El botón debe quedar en azul (true) o gris (false)?
        respuesta.put("count", count); // Número: ¿Cuántos likes totales mostramos al lado del botón?
        
        return ResponseEntity.ok(respuesta); // Devolvemos el código 200 OK con la respuesta
    }

    /**
     * Endpoint para obtener simplemente el NÚMERO TOTAL de likes de algo.
     * @GetMapping para leer datos.
     * URL: GET /api/likes/count/{tipo}/{entidadId} (ej. /api/likes/count/post/5)
     */
    @GetMapping("/count/{tipo}/{entidadId}")
    public ResponseEntity<Long> countLikes(@PathVariable String tipo, @PathVariable Long entidadId) {
        // Simplemente contamos en base de datos y devolvemos el número.
        return ResponseEntity.ok(likeRepository.countByTipoAndEntidadId(tipo, entidadId));
    }

    /**
     * Endpoint para comprobar si un usuario concreto YA LE HA DADO LIKE a algo.
     * Muy útil al cargar la página para pintar el botón de corazón rojo o gris.
     * URL: GET /api/likes/check/{usuarioId}/{tipo}/{entidadId}
     */
    @GetMapping("/check/{usuarioId}/{tipo}/{entidadId}")
    public ResponseEntity<Boolean> checkLike(@PathVariable Long usuarioId, 
                                            @PathVariable String tipo, 
                                            @PathVariable Long entidadId) {
        // 'existsBy...' es una magia de Spring Data JPA que devuelve true o false si encuentra al menos un registro.
        return ResponseEntity.ok(likeRepository
            .existsByUsuarioIdAndTipoAndEntidadId(usuarioId, tipo, entidadId));
    }
}
