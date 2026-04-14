package thirty_api.controllers;

import org.springframework.http.ResponseEntity;
import thirty_api.models.Post;
import thirty_api.repositories.PostRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class PostController {

    private final PostRepository postRepository;

    // Constructor para que Spring inyecte el repositorio
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Listar todos los posts en el muro
    @GetMapping
    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // Guardar un nuevo post
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        return postRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
