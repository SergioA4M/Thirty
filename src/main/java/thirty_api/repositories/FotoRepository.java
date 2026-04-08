package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import thirty_api.models.Foto;
import java.util.List;

public interface FotoRepository extends JpaRepository<Foto, Long> {
    List<Foto> findByUsuarioId(Long usuarioId);
}
