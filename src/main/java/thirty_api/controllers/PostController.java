package thirty_api.controllers;

import org.springframework.http.ResponseEntity;
import thirty_api.models.Post;
import thirty_api.repositories.PostRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de Publicaciones (PostController).
 * Maneja lo que la gente escribe en los muros (los posts o "novedades").
 */
@RestController // Controlador que devuelve respuestas JSON
@RequestMapping("/api/posts") // Ruta principal para posts
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class PostController {

    // Repositorio de la base de datos de Posts
    private final PostRepository postRepository;

    /**
     * Constructor para que Spring inyecte autom\u00e1ticamente el repositorio al arrancar.
     * Es una alternativa al @Autowired.
     */
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Listar todas las publicaciones del muro, ordenadas de m\u00e1s nueva a m\u00e1s vieja.
     * Endpoint: GET /api/posts
     */
    @GetMapping
    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Guarda una nueva publicaci\u00f3n en la base de datos.
     * Endpoint: POST /api/posts
     */
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        // Le ponemos la fecha y hora exactas de ahora mismo
        post.setCreatedAt(LocalDateTime.now());
        // Lo guardamos
        return postRepository.save(post);
    }
    
    /**
     * Elimina un post de la base de datos a partir de su ID.
     * Endpoint: DELETE /api/posts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.ok().build(); // Devuelve un 200 OK (sin cuerpo/texto)
    }

    /**
     * Busca un post concreto por su ID (Muy \u00fatil para la p\u00e1gina de ver un post individual para compartir).
     * Endpoint: GET /api/posts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        // Busca el post. Si lo encuentra, lo devuelve (ok). Si no, devuelve un 404 (notFound).
        return postRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
