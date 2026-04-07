package thirty_api.repositories;

import thirty_api.models.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    @Query("SELECT m FROM Mensaje m WHERE (m.emisor = :u1 AND m.receptor = :u2) OR (m.emisor = :u2 AND m.receptor = :u1) ORDER BY m.fecha ASC")
    List<Mensaje> findChat(@Param("u1") String u1, @Param("u2") String u2);
}
