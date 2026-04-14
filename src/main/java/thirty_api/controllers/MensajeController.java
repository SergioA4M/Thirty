package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Mensaje;
import thirty_api.models.Notificacion;
import thirty_api.repositories.MensajeRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.NotificacionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class MensajeController {

    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    @PostMapping("/enviar")
    public Mensaje enviar(@RequestParam Long emisorId, @RequestParam Long receptorId, @RequestBody String texto) {
        Mensaje m = new Mensaje();
        m.setEmisor(userRepository.findById(emisorId).get());
        m.setReceptor(userRepository.findById(receptorId).get());
        m.setContenido(texto);
        mensajeRepository.save(m);

        Notificacion notif = new Notificacion();
        notif.setUsuario(m.getReceptor());
        notif.setEmisor(m.getEmisor());
        notif.setTipo("mensaje");
        notif.setContenido(m.getEmisor().getFirstName() + " te envió un mensaje");
        notif.setEntidadId(m.getId());
        notificacionRepository.save(notif);

        return m;
    }

    @GetMapping("/historial/{id1}/{id2}")
    public List<Mensaje> historial(@PathVariable Long id1, @PathVariable Long id2) {
        return mensajeRepository.buscarConversacion(id1, id2);
    }

    @PostMapping("/marcar-leidos/{emisorId}/{receptorId}")
    public void marcarLeidos(@PathVariable Long emisorId, @PathVariable Long receptorId) {
        List<Mensaje> mensajes = mensajeRepository.buscarConversacion(emisorId, receptorId);
        mensajes.stream()
            .filter(m -> m.getReceptor().getId().equals(emisorId) && !m.isLeido())
            .forEach(m -> {
                m.setLeido(true);
                mensajeRepository.save(m);
            });
    }
}
