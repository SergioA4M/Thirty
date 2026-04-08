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
        return comentarioRepository.save(c);
    }

    @GetMapping("/perfil/{perfilId}")
    public List<Comentario> getComentarios(@PathVariable Long perfilId) {
        return comentarioRepository.findByPerfilIdOrderByFechaDesc(perfilId);
    }
}
