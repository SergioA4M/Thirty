package thirty_api.repositories; // El que use tu proyecto

import thirty_api.models.Post; // Asegúrate de que esta ruta sea la de tu carpeta model
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
}