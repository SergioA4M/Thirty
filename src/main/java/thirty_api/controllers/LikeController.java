package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thirty_api.models.Like;
import thirty_api.models.User;
import thirty_api.models.Notificacion;
import thirty_api.repositories.LikeRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.PostRepository;
import thirty_api.repositories.NotificacionRepository;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/likes")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class LikeController {

    @Autowired private LikeRepository likeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(@RequestBody Map<String, Object> payload) {
        Long usuarioId = ((Number) payload.get("usuarioId")).longValue();
        Long entidadId = ((Number) payload.get("entidadId")).longValue();
        String tipo = (String) payload.get("tipo");

        Optional<Like> likeExistente = likeRepository
            .findByUsuarioIdAndTipoAndEntidadId(usuarioId, tipo, entidadId);

        boolean liked;
        if (likeExistente.isPresent()) {
            likeRepository.delete(likeExistente.get());
            liked = false;
        } else {
            User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Like nuevoLike = new Like();
            nuevoLike.setUsuario(usuario);
            nuevoLike.setTipo(tipo);
            nuevoLike.setEntidadId(entidadId);
            likeRepository.save(nuevoLike);
            liked = true;

            if ("post".equals(tipo)) {
                Notificacion notif = new Notificacion();
                notif.setEmisor(usuario);
                notif.setTipo("like");
                notif.setContenido(usuario.getFirstName() + " le dio like a tu publicación");
                notif.setEntidadId(entidadId);
                notificacionRepository.save(notif);
            }
        }

        long count = likeRepository.countByTipoAndEntidadId(tipo, entidadId);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("liked", liked);
        respuesta.put("count", count);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/count/{tipo}/{entidadId}")
    public ResponseEntity<Long> countLikes(@PathVariable String tipo, @PathVariable Long entidadId) {
        return ResponseEntity.ok(likeRepository.countByTipoAndEntidadId(tipo, entidadId));
    }

    @GetMapping("/check/{usuarioId}/{tipo}/{entidadId}")
    public ResponseEntity<Boolean> checkLike(@PathVariable Long usuarioId, 
                                            @PathVariable String tipo, 
                                            @PathVariable Long entidadId) {
        return ResponseEntity.ok(likeRepository
            .existsByUsuarioIdAndTipoAndEntidadId(usuarioId, tipo, entidadId));
    }
}
