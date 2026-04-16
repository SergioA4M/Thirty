package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Notificacion;
import thirty_api.models.User;
import thirty_api.repositories.NotificacionRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.MensajeRepository;
import thirty_api.repositories.ComentarioRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controlador de Notificaciones (NotificacionController).
 *
 * ¿Qué es esto?
 * En Spring Boot, un @RestController maneja las peticiones que llegan desde el Frontend.
 * Este controlador es el encargado de gestionar las "alertas" o notificaciones que 
 * recibe un usuario (ej. "A Juan le ha gustado tu foto", "María te ha comentado en el muro", 
 * mensajes nuevos, etc.). Es clave para mantener a los usuarios informados y enganchados.
 */
@RestController // Indica que esta clase devuelve datos (JSON), no vistas HTML directamente.
@RequestMapping("/api/notificaciones") // Todas las URLs de este controlador empiezan por "/api/notificaciones"
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite peticiones desde el frontend web
public class NotificacionController {

    // @Autowired inyecta (proporciona automáticamente) los repositorios necesarios para acceder a la base de datos.
    @Autowired private NotificacionRepository notificacionRepository; // Para leer/guardar notificaciones
    @Autowired private UserRepository userRepository;                 // Para buscar usuarios implicados
    @Autowired private MensajeRepository mensajeRepository;           // Para contar mensajes sin leer
    @Autowired private ComentarioRepository comentarioRepository;     // Para contar comentarios no leídos

    /**
     * Endpoint para obtener TODAS las notificaciones de un usuario.
     * @GetMapping responde a peticiones GET (para leer o pedir datos).
     * URL: GET /api/notificaciones/{usuarioId}
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones(@PathVariable Long usuarioId) {
        // Buscamos todas las notificaciones del usuario ordenadas por fecha (las más recientes primero).
        List<Notificacion> notificaciones = notificacionRepository
            .findByUsuarioIdOrderByFechaDesc(usuarioId);
        // Devolvemos la lista con un código 200 (OK).
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Endpoint para obtener solo las notificaciones SIN LEER.
     * Utilizado habitualmente para mostrar un globo rojo con el número en la barra de navegación.
     * URL: GET /api/notificaciones/sin-leer/{usuarioId}
     */
    @GetMapping("/sin-leer/{usuarioId}")
    public ResponseEntity<List<Notificacion>> notificacionesSinLeer(@PathVariable Long usuarioId) {
        // Pedimos al repositorio que filtre por ID de usuario y donde 'leido' sea falso.
        List<Notificacion> notificaciones = notificacionRepository
            .findByUsuarioIdAndLeidoFalseOrderByFechaDesc(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Endpoint para obtener un resumen estadístico de todo lo que el usuario tiene pendiente de ver.
     * Muy útil para la barra superior del Frontend (íconos de mensajes, notificaciones, etc.).
     * URL: GET /api/notificaciones/resumen/{usuarioId}
     */
    @GetMapping("/resumen/{usuarioId}")
    public ResponseEntity<Map<String, Object>> resumen(@PathVariable Long usuarioId) {
        // Usamos un Map (Diccionario) para devolver diferentes contadores agrupados en un solo objeto JSON.
        Map<String, Object> resumen = new HashMap<>();

        // Hacemos consultas (Count) a la base de datos para saber cuántos elementos hay sin leer de cada tipo.
        long mensajes = mensajeRepository.countByReceptorIdAndLeidoFalse(usuarioId);
        long comentarios = notificacionRepository.countByUsuarioIdAndTipoAndLeidoFalse(usuarioId, "comentario");
        long likes = notificacionRepository.countByUsuarioIdAndTipoAndLeidoFalse(usuarioId, "like");
        long solicitudes = notificacionRepository.countByUsuarioIdAndTipoAndLeidoFalse(usuarioId, "solicitud_amistad");

        // Rellenamos el mapa con los resultados
        resumen.put("mensajesNuevos", mensajes);
        resumen.put("comentariosNuevos", comentarios);
        resumen.put("likesNuevos", likes);
        resumen.put("notificacionesTotales", mensajes + comentarios + likes + solicitudes); // La suma total
        resumen.put("solicitudesAmistad", solicitudes);

        // Devolvemos el mapa convertido en JSON
        return ResponseEntity.ok(resumen);
    }

    /**
     * Endpoint para marcar una notificación concreta como leída.
     * Se llama cuando el usuario hace clic sobre una notificación específica.
     * URL: POST /api/notificaciones/marcar-leida/{id}
     */
    @PostMapping("/marcar-leida/{id}")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        try {
            // Buscamos la notificación por su ID. Si está presente (ifPresent), ejecutamos el bloque de código.
            notificacionRepository.findById(id).ifPresent(notif -> {
                notif.setLeido(true); // Cambiamos el estado a leído (true)
                notificacionRepository.save(notif); // Sobrescribimos en la base de datos
            });
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            // Capturamos cualquier error para evitar que la app se caiga, pero devolvemos OK para no romper el flujo.
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Endpoint para marcar TODAS las notificaciones de un usuario como leídas.
     * Se llama cuando el usuario abre el menú desplegable de notificaciones para "limpiarlo".
     * URL: POST /api/notificaciones/marcar-todas-leidas/{usuarioId}
     */
    @PostMapping("/marcar-todas-leidas/{usuarioId}")
    public ResponseEntity<?> marcarTodasLeidas(@PathVariable Long usuarioId) {
        try {
            // 1. Buscamos todas las notificaciones no leídas del usuario.
            List<Notificacion> notifs = notificacionRepository.findByUsuarioIdAndLeidoFalseOrderByFechaDesc(usuarioId);
            
            // 2. Iteramos (recorremos) sobre cada notificación y la marcamos como leída.
            notifs.forEach(n -> {
                n.setLeido(true);
                notificacionRepository.save(n); // Guardamos el cambio de cada una
            });
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Endpoint manual para crear una notificación personalizada.
     * A veces el Frontend necesita enviar una notificación específica que no se genera automáticamente 
     * al crear un comentario o un like.
     * URL: POST /api/notificaciones/crear
     */
    @PostMapping("/crear")
    public ResponseEntity<Notificacion> crearNotificacion(@RequestBody Map<String, Object> payload) {
        // 1. Extraemos los datos del JSON que envía el Frontend (payload).
        Long usuarioId = ((Number) payload.get("usuarioId")).longValue();
        Long emisorId = ((Number) payload.get("emisorId")).longValue();
        String tipo = (String) payload.get("tipo");
        String contenido = (String) payload.get("contenido");
        
        // El ID de entidad es opcional (por ejemplo, el ID de un post para saber a dónde ir al hacer clic).
        Long entidadId = payload.containsKey("entidadId") ? 
            ((Number) payload.get("entidadId")).longValue() : null;

        // 2. Buscamos a los usuarios implicados. Lanzamos error si no existen.
        User usuario = userRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User emisor = userRepository.findById(emisorId)
            .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        // 3. Creamos el objeto Notificacion y lo rellenamos.
        Notificacion notif = new Notificacion();
        notif.setUsuario(usuario); // El que la recibe
        notif.setEmisor(emisor);   // El que la causa
        notif.setTipo(tipo);       // "like", "comentario", "etiqueta"...
        notif.setContenido(contenido);
        if (entidadId != null) notif.setEntidadId(entidadId);

        // 4. Guardamos en la base de datos y devolvemos la notificación creada.
        return ResponseEntity.ok(notificacionRepository.save(notif));
    }
}
