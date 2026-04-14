package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private User emisor;

    @Column(nullable = false)
    private String tipo; // "mensaje", "solicitud_amistad", "comentario", "like", "story"

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private Long entidadId; // ID del post, comentario, etc.

    private boolean leido = false;

    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}
