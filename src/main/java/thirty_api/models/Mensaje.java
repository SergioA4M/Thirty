package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private User emisor;

    @ManyToOne
    @JoinColumn(name = "receptor_id")
    private User receptor;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private LocalDateTime fechaEnvio;

    // AÑADE ESTA LÍNEA:
    private boolean leido = false;

    public Mensaje() {
        this.fechaEnvio = LocalDateTime.now();
    }
}
