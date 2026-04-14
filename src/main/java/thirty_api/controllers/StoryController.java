package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import thirty_api.models.Story;
import thirty_api.models.ComentarioStory;
import thirty_api.models.User;
import thirty_api.repositories.StoryRepository;
import thirty_api.repositories.ComentarioStoryRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.NotificacionRepository;
import thirty_api.models.Notificacion;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stories")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class StoryController {

    @Autowired private StoryRepository storyRepository;
    @Autowired private ComentarioStoryRepository comentarioStoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    @PostMapping("/crear/{usuarioId}")
    public ResponseEntity<?> crearStory(@PathVariable Long usuarioId, 
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String texto) {
        try {
            User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String extension = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = "story_" + System.currentTimeMillis() + extension;

            Path ruta = Paths.get("uploads").resolve(nombreArchivo);
            Files.copy(file.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

            Story story = new Story();
            story.setUsuario(usuario);
            story.setImagenUrl(nombreArchivo);
            story.setTexto(texto);
            storyRepository.save(story);

            return ResponseEntity.ok(story);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/feed/{usuarioId}")
    public ResponseEntity<List<Story>> obtenerFeed(@PathVariable Long usuarioId) {
        try {
            User usuario = userRepository.findById(usuarioId).orElse(null);
            if (usuario == null) {
                return ResponseEntity.ok(List.of());
            }

            LocalDateTime ahora = LocalDateTime.now();
            List<Long> amigoIds;
            if (usuario.getAmigos() != null && !usuario.getAmigos().isEmpty()) {
                amigoIds = usuario.getAmigos().stream()
                    .map(User::getId)
                    .toList();
            } else {
                amigoIds = List.of();
            }
            amigoIds.add(usuarioId);

            List<Story> stories = storyRepository.findStoriesDeAmigos(amigoIds, ahora);
            return ResponseEntity.ok(stories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/mis-stories/{usuarioId}")
    public ResponseEntity<List<Story>> misStories(@PathVariable Long usuarioId) {
        List<Story> stories = storyRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/{storyId}/comentarios")
    public ResponseEntity<List<ComentarioStory>> comentariosStory(@PathVariable Long storyId) {
        List<ComentarioStory> comentarios = comentarioStoryRepository
            .findByStoryIdOrderByFechaAsc(storyId);
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping("/{storyId}/comentar/{usuarioId}")
    public ResponseEntity<?> comentarStory(@PathVariable Long storyId, 
                                          @PathVariable Long usuarioId,
                                          @RequestBody String contenido) {
        try {
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story no encontrado"));
            User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            ComentarioStory comentario = new ComentarioStory();
            comentario.setStory(story);
            comentario.setUsuario(usuario);
            comentario.setContenido(contenido);
            comentarioStoryRepository.save(comentario);

            if (!usuarioId.equals(story.getUsuario().getId())) {
                Notificacion notif = new Notificacion();
                notif.setUsuario(story.getUsuario());
                notif.setEmisor(usuario);
                notif.setTipo("comentario_story");
                notif.setContenido(usuario.getFirstName() + " comentou tu story");
                notif.setEntidadId(storyId);
                notificacionRepository.save(notif);
            }

            return ResponseEntity.ok(comentario);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<?> eliminarStory(@PathVariable Long storyId) {
        storyRepository.deleteById(storyId);
        return ResponseEntity.ok("Story eliminado");
    }
}
