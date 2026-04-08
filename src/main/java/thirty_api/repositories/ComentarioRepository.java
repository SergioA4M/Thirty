package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import thirty_api.models.Comentario;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    // Buscamos los comentarios de un perfil ordenados por fecha (el más nuevo arriba)
    List<Comentario> findByPerfilIdOrderByFechaDesc(Long perfilId);
}