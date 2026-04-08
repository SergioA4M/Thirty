package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import thirty_api.models.Mensaje;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Esta consulta busca la conversación entre dos usuarios (en ambos sentidos)
    @Query("SELECT m FROM Mensaje m WHERE (m.emisor.id = ?1 AND m.receptor.id = ?2) OR (m.emisor.id = ?2 AND m.receptor.id = ?1) ORDER BY m.fechaEnvio ASC")
    List<Mensaje> buscarConversacion(Long id1, Long id2);
}
