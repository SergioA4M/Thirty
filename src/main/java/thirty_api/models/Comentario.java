package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private User autor; // El que escribe

    @ManyToOne
    @JoinColumn(name = "perfil_id")
    private User perfil; // El dueño del muro

    // AÑADE ESTO para que la notificación funcione de verdad
    private boolean leido = false;
}
