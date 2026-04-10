package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Comentario;
import thirty_api.models.User;
import thirty_api.repositories.ComentarioRepository;
import thirty_api.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin("*")
public class ComentarioController {

    @Autowired private ComentarioRepository comentarioRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/{perfilId}/{autorId}")
    public Comentario publicar(@PathVariable Long perfilId, @PathVariable Long autorId, @RequestBody String texto) {
        User perfil = userRepository.findById(perfilId).get();
        User autor = userRepository.findById(autorId).get();

        Comentario c = new Comentario();
        c.setContenido(texto);
        c.setPerfil(perfil);
        c.setAutor(autor);
        c.setLeido(false); // <--- IMPORTANTE: Nace como notificación pendiente
        return comentarioRepository.save(c);
    }

    @GetMapping("/perfil/{perfilId}")
    public List<Comentario> getComentarios(@PathVariable Long perfilId) {
        // --- EXTRA: LIMPIAR NOTIFICACIONES ---
        // Cuando alguien pide los comentarios de SU PROPIO perfil, los marcamos como leídos
        List<Comentario> lista = comentarioRepository.findByPerfilIdOrderByFechaDesc(perfilId);

        // Si quieres que solo se marquen como leídos cuando el DUEÑO los ve,
        // podrías añadir una lógica aquí, pero por ahora vamos a marcarlos todos:
        lista.forEach(com -> {
            if(!com.isLeido()){
                com.setLeido(true);
                comentarioRepository.save(com);
            }
        });

        return lista;
    }
}
