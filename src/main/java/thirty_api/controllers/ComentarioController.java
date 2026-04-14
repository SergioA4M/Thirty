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

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class ComentarioController {

    @Autowired private ComentarioRepository comentarioRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    @PostMapping("/{perfilId}/{autorId}")
    public Comentario publicar(@PathVariable Long perfilId, @PathVariable Long autorId, @RequestBody String texto) {
        User perfil = userRepository.findById(perfilId).get();
        User autor = userRepository.findById(autorId).get();

        Comentario c = new Comentario();
        c.setContenido(texto);
        c.setPerfil(perfil);
        c.setAutor(autor);
        c.setLeido(false);
        Comentario guardado = comentarioRepository.save(c);

        if (!perfilId.equals(autorId)) {
            Notificacion notif = new Notificacion();
            notif.setUsuario(perfil);
            notif.setEmisor(autor);
            notif.setTipo("comentario");
            notif.setContenido(autor.getFirstName() + " comentó en tu perfil");
            notif.setEntidadId(guardado.getId());
            notificacionRepository.save(notif);
        }

        return guardado;
    }

    @GetMapping("/perfil/{perfilId}")
    public List<Comentario> getComentarios(@PathVariable Long perfilId) {
        List<Comentario> lista = comentarioRepository.findByPerfilIdOrderByFechaDesc(perfilId);
        lista.forEach(com -> {
            if (!com.isLeido()) {
                com.setLeido(true);
                comentarioRepository.save(com);
            }
        });
        return lista;
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        comentarioRepository.deleteById(id);
    }
}
