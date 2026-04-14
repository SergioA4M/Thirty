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

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class NotificacionController {

    @Autowired private NotificacionRepository notificacionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private ComentarioRepository comentarioRepository;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones(@PathVariable Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository
            .findByUsuarioIdOrderByFechaDesc(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/sin-leer/{usuarioId}")
    public ResponseEntity<List<Notificacion>> notificacionesSinLeer(@PathVariable Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository
            .findByUsuarioIdAndLeidoFalseOrderByFechaDesc(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/resumen/{usuarioId}")
    public ResponseEntity<Map<String, Object>> resumen(@PathVariable Long usuarioId) {
        Map<String, Object> resumen = new HashMap<>();

        long mensajes = mensajeRepository.countByReceptorIdAndLeidoFalse(usuarioId);
        long comentarios = comentarioRepository.countByPerfilIdAndLeidoFalse(usuarioId);
        long notificacionesBD = notificacionRepository.countByUsuarioIdAndLeidoFalse(usuarioId);
        long solicitudes = notificacionRepository.countByUsuarioIdAndTipoAndLeidoFalse(usuarioId, "solicitud_amistad");

        resumen.put("mensajesNuevos", mensajes);
        resumen.put("comentariosNuevos", comentarios);
        resumen.put("notificacionesTotales", mensajes + comentarios + notificacionesBD);
        resumen.put("solicitudesAmistad", solicitudes);

        return ResponseEntity.ok(resumen);
    }

    @PostMapping("/marcar-leida/{id}")
    public ResponseEntity<?> marcarLeida(@PathVariable Long id) {
        notificacionRepository.marcarLeida(id);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/marcar-todas-leidas/{usuarioId}")
    public ResponseEntity<?> marcarTodasLeidas(@PathVariable Long usuarioId) {
        notificacionRepository.marcarTodasLeidas(usuarioId);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/crear")
    public ResponseEntity<Notificacion> crearNotificacion(@RequestBody Map<String, Object> payload) {
        Long usuarioId = ((Number) payload.get("usuarioId")).longValue();
        Long emisorId = ((Number) payload.get("emisorId")).longValue();
        String tipo = (String) payload.get("tipo");
        String contenido = (String) payload.get("contenido");
        Long entidadId = payload.containsKey("entidadId") ? 
            ((Number) payload.get("entidadId")).longValue() : null;

        User usuario = userRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User emisor = userRepository.findById(emisorId)
            .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        Notificacion notif = new Notificacion();
        notif.setUsuario(usuario);
        notif.setEmisor(emisor);
        notif.setTipo(tipo);
        notif.setContenido(contenido);
        if (entidadId != null) notif.setEntidadId(entidadId);

        return ResponseEntity.ok(notificacionRepository.save(notif));
    }
}
