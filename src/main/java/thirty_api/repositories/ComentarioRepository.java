package thirty_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import thirty_api.models.Comentario;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // Para mostrar el tablón en el perfil
    List<Comentario> findByPerfilIdOrderByFechaDesc(Long perfilId);

    // PARA LAS NOTIFICACIONES: Cuenta solo los que el usuario no ha visto aún
    // Importante: El nombre debe ser exacto para que Spring JPA lo reconozca
    long countByPerfilIdAndLeidoFalse(Long perfilId);
}