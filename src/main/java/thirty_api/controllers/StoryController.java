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

/**
 * Controlador de Stories (StoryController)
 * Maneja la creaci\u00f3n, visualizaci\u00f3n, eliminaci\u00f3n y comentarios
 * de las publicaciones ef\u00edmeras estilo "Instagram Stories".
 */
@RestController // Indica que devuelve respuestas en formato JSON
@RequestMapping("/api/stories") // Ruta base para estos endpoints
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite el acceso al Frontend
public class StoryController {

    // Repositorios para conectar con la base de datos
    @Autowired private StoryRepository storyRepository;
    @Autowired private ComentarioStoryRepository comentarioStoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    // Servicio para subir archivos a Supabase Storage
    @Autowired
    private thirty_api.services.SupabaseService supabaseService;

    /**
     * Crea una nueva Story (Foto + opcionalmente un texto)
     * Endpoint: POST /api/stories/crear/{usuarioId}
     */
    @PostMapping("/crear/{usuarioId}")
    public ResponseEntity<?> crearStory(@PathVariable Long usuarioId, 
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String texto) {
        try {
            // Buscamos al usuario que est\u00e1 subiendo la story
            User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Generamos un nombre \u00fanico basado en la fecha para no sobreescribir otras fotos
            String extension = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = "story_" + System.currentTimeMillis() + extension;

            // Sube el archivo a tu Supabase Storage en vez de guardarlo en el disco local
            supabaseService.uploadFile(file.getBytes(), nombreArchivo, file.getContentType());

            // Creamos el objeto Story y lo guardamos en base de datos
            Story story = new Story();
            story.setUsuario(usuario);
            story.setImagenUrl(nombreArchivo);
            story.setTexto(texto);
            storyRepository.save(story);

            return ResponseEntity.ok(story); // Todo OK, devolvemos la story creada
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage()); // Algo fall\u00f3
        }
    }

    /**
     * Obtiene el listado de stories activas de tus amigos y las tuyas propias
     * Endpoint: GET /api/stories/feed/{usuarioId}
     */
    @GetMapping("/feed/{usuarioId}")
    public ResponseEntity<List<Story>> obtenerFeed(@PathVariable Long usuarioId) {
        try {
            User usuario = userRepository.findById(usuarioId).orElse(null);
            if (usuario == null) {
                return ResponseEntity.ok(List.of()); // Si no hay usuario, lista vac\u00eda
            }

            LocalDateTime ahora = LocalDateTime.now();
            List<Long> amigoIds = new java.util.ArrayList<>();
            
            // Recopilamos todos los IDs de tus amigos
            if (usuario.getAmigos() != null && !usuario.getAmigos().isEmpty()) {
                amigoIds.addAll(usuario.getAmigos().stream()
                    .map(User::getId)
                    .toList());
            }
            // Tambi\u00e9n nos a\u00f1adimos a nosotros mismos para ver nuestras stories
            amigoIds.add(usuarioId);

            // Buscamos en BD las stories de esa lista de IDs
            List<Story> stories = storyRepository.findStoriesDeAmigos(amigoIds, ahora);
            return ResponseEntity.ok(stories);
        } catch (Exception e) {
            System.err.println("Error en obtenerFeed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * Obtiene \u00faniamente las stories publicadas por un usuario espec\u00edfico
     * Endpoint: GET /api/stories/mis-stories/{usuarioId}
     */
    @GetMapping("/mis-stories/{usuarioId}")
    public ResponseEntity<List<Story>> misStories(@PathVariable Long usuarioId) {
        List<Story> stories = storyRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return ResponseEntity.ok(stories);
    }

    /**
     * Obtiene todos los comentarios de una story en particular
     * Endpoint: GET /api/stories/{storyId}/comentarios
     */
    @GetMapping("/{storyId}/comentarios")
    public ResponseEntity<List<ComentarioStory>> comentariosStory(@PathVariable Long storyId) {
        List<ComentarioStory> comentarios = comentarioStoryRepository
            .findByStoryIdOrderByFechaAsc(storyId);
        return ResponseEntity.ok(comentarios);
    }

    /**
     * Publica un comentario en una story y env\u00eda una notificaci\u00f3n al due\u00f1o de la story
     * Endpoint: POST /api/stories/{storyId}/comentar/{usuarioId}
     */
    @PostMapping("/{storyId}/comentar/{usuarioId}")
    public ResponseEntity<?> comentarStory(@PathVariable Long storyId, 
                                          @PathVariable Long usuarioId,
                                          @RequestBody String contenido) {
        try {
            // Buscar la story a la que queremos comentar
            Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story no encontrado"));
            
            // Buscar qui\u00e9n es el autor del comentario
            User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Guardar el comentario en la base de datos
            ComentarioStory comentario = new ComentarioStory();
            comentario.setStory(story);
            comentario.setUsuario(usuario);
            comentario.setContenido(contenido);
            comentarioStoryRepository.save(comentario);

            // Si le comento la story a otro (y no a m\u00ed mismo), le env\u00edo notificaci\u00f3n
            if (!usuarioId.equals(story.getUsuario().getId())) {
                Notificacion notif = new Notificacion();
                notif.setUsuario(story.getUsuario()); // El receptor (due\u00f1o de la story)
                notif.setEmisor(usuario);             // Yo (el que comenta)
                notif.setTipo("comentario_story");
                notif.setContenido(usuario.getFirstName() + " comentou tu story"); // Texto del aviso
                notif.setEntidadId(storyId);
                notificacionRepository.save(notif);
            }

            return ResponseEntity.ok(comentario);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Borra una story permanentemente
     * Endpoint: DELETE /api/stories/{storyId}
     */
    @DeleteMapping("/{storyId}")
    public ResponseEntity<?> eliminarStory(@PathVariable Long storyId) {
        storyRepository.deleteById(storyId);
        return ResponseEntity.ok("Story eliminado");
    }
}
