package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import thirty_api.models.Notificacion;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    
    List<Notificacion> findByUsuarioIdAndLeidoFalseOrderByFechaDesc(Long usuarioId);
    
    long countByUsuarioIdAndLeidoFalse(Long usuarioId);
    
    long countByUsuarioIdAndTipoAndLeidoFalse(Long usuarioId, String tipo);
    
    @Modifying
    @Query("UPDATE Notificacion n SET n.leido = true WHERE n.usuario.id = :usuarioId")
    void marcarTodasLeidas(Long usuarioId);
    
    @Modifying
    @Query("UPDATE Notificacion n SET n.leido = true WHERE n.id = :id")
    void marcarLeida(Long id);
}
