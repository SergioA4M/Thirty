package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Mensaje;
import thirty_api.repositories.MensajeRepository;
import thirty_api.repositories.UserRepository;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*")
public class MensajeController {

    @Autowired private MensajeRepository mensajeRepository;
    @Autowired private UserRepository userRepository;

    // Enviar un mensaje
    @PostMapping("/enviar")
    public Mensaje enviar(@RequestParam Long emisorId, @RequestParam Long receptorId, @RequestBody String texto) {
        Mensaje m = new Mensaje();
        m.setEmisor(userRepository.findById(emisorId).get());
        m.setReceptor(userRepository.findById(receptorId).get());
        m.setContenido(texto);
        return mensajeRepository.save(m);
    }

    // Leer la conversación
    @GetMapping("/historial/{id1}/{id2}")
    public List<Mensaje> historial(@PathVariable Long id1, @PathVariable Long id2) {
        return mensajeRepository.buscarConversacion(id1, id2);
    }
}
