package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thirty_api.models.Like;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    
    Optional<Like> findByUsuarioIdAndTipoAndEntidadId(Long usuarioId, String tipo, Long entidadId);
    
    long countByTipoAndEntidadId(String tipo, Long entidadId);
    
    boolean existsByUsuarioIdAndTipoAndEntidadId(Long usuarioId, String tipo, Long entidadId);
    
    List<Like> findByTipoAndEntidadIdOrderByFechaDesc(String tipo, Long entidadId);
}
