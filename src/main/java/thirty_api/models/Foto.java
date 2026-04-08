package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Foto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url; // Nombre del archivo en /uploads/
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario; // El dueño del álbum
}
