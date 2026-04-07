package thirty_api.controllers;

import thirty_api.models.Mensaje;
import thirty_api.repositories.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*")
public class MensajeController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @PostMapping
    public Mensaje enviarMensaje(@RequestBody Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }

    @GetMapping("/{u1}/{u2}")
    public List<Mensaje> obtenerChat(@PathVariable String u1, @PathVariable String u2) {
        return mensajeRepository.findChat(u1, u2);
    }
}
