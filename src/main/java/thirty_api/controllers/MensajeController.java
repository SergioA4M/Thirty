package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Mensaje;
import thirty_api.models.Notificacion;
import thirty_api.repositories.MensajeRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.NotificacionRepository;

import java.util.List;

/**
 * Controlador de Mensajes (Chat).
 * Gestiona el env\u00edo y recepci\u00f3n de mensajes privados entre usuarios, as\u00ed como sus notificaciones.
 */
@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class MensajeController {

    // Repositorios para guardar datos en las tablas correspondientes
    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    /**
     * Env\u00eda un mensaje privado de un usuario a otro y genera una notificaci\u00f3n.
     * Endpoint: POST /api/mensajes/enviar?emisorId=1&receptorId=2
     */
    @PostMapping("/enviar")
    public Mensaje enviar(@RequestParam Long emisorId, @RequestParam Long receptorId, @RequestBody String texto) {
        // Preparamos el mensaje
        Mensaje m = new Mensaje();
        m.setEmisor(userRepository.findById(emisorId).get());
        m.setReceptor(userRepository.findById(receptorId).get());
        m.setContenido(texto);
        mensajeRepository.save(m);

        // Generamos una notificaci\u00f3n para que el receptor sepa que tiene un mensaje nuevo
        Notificacion notif = new Notificacion();
        notif.setUsuario(m.getReceptor());
        notif.setEmisor(m.getEmisor());
        notif.setTipo("mensaje");
        notif.setContenido(m.getEmisor().getFirstName() + " te envi\u00f3 un mensaje");
        notif.setEntidadId(m.getId());
        notificacionRepository.save(notif);

        return m; // Devolvemos el mensaje guardado al frontend
    }

    /**
     * Devuelve el historial completo de mensajes entre dos personas.
     * Endpoint: GET /api/mensajes/historial/{id1}/{id2}
     */
    @GetMapping("/historial/{id1}/{id2}")
    public List<Mensaje> historial(@PathVariable Long id1, @PathVariable Long id2) {
        // Busca la charla sin importar qui\u00e9n la empez\u00f3 (A y B)
        return mensajeRepository.buscarConversacion(id1, id2);
    }

    /**
     * Marca como "le\u00eddos" todos los mensajes que ten\u00eda pendientes de una conversaci\u00f3n.
     * Esto limpia el globo verde de notificaciones de chat.
     * Endpoint: POST /api/mensajes/marcar-leidos/{emisorId}/{receptorId}
     */
    @PostMapping("/marcar-leidos/{emisorId}/{receptorId}")
    public void marcarLeidos(@PathVariable Long emisorId, @PathVariable Long receptorId) {
        // Obtenemos todos los mensajes de la charla
        List<Mensaje> mensajes = mensajeRepository.buscarConversacion(emisorId, receptorId);
        
        // Filtramos los que est\u00e1n sin leer y de los que yo soy el receptor, y los marcamos como le\u00eddos (true)
        mensajes.stream()
            .filter(m -> m.getReceptor().getId().equals(emisorId) && !m.isLeido())
            .forEach(m -> {
                m.setLeido(true);
                mensajeRepository.save(m);
            });
    }
}
