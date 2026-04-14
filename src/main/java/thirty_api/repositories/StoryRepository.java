package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thirty_api.models.Story;
import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    
    @Query("SELECT s FROM Story s WHERE s.usuario.id = :usuarioId AND s.fechaExpiracion > :ahora ORDER BY s.fechaCreacion DESC")
    List<Story> findStoriesActivasPorUsuario(Long usuarioId, LocalDateTime ahora);
    
    @Query("SELECT s FROM Story s WHERE s.usuario.id IN :amigoIds AND s.fechaExpiracion > :ahora ORDER BY s.fechaCreacion DESC")
    List<Story> findStoriesDeAmigos(List<Long> amigoIds, LocalDateTime ahora);
    
    @Query("SELECT s FROM Story s WHERE s.fechaExpiracion > :ahora ORDER BY s.fechaCreacion DESC")
    List<Story> findAllActivas(LocalDateTime ahora);
    
    List<Story> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
