package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad 'Story': Implementa la popular funcionalidad de "Historias efímeras".
 * 
 * Aunque no es una característica puramente "clásica" como los tablones, 
 * incorporar historias (contenidos que duran 24 horas) en Thirty sirve 
 * para modernizar un poco la experiencia de la red social mientras se mantiene
 * el estilo retro. Además, demuestra la capacidad técnica para manejar expiración de datos temporales (Time-to-Live).
 */
@Entity // Entidad JPA que se guarda en la base de datos PostgreSQL.
@Data // Anotación de Lombok para autogenerar getters, setters y constructores.
@Table(name = "stories") // Especificamos el nombre explícito para la tabla (las bases de datos prefieren nombres en minúscula y plurales).
public class Story {

    /**
     * Identificador autoincremental de la historia (Clave primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación Mucho a Uno: Muchos Stories (historias) pueden pertenecer a un solo Usuario.
     * En la base de datos se crea una columna 'usuario_id' (Foreign Key) 
     * que apunta al 'id' de la tabla 'users'.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /**
     * URL que apunta a la imagen de la historia almacenada en Supabase Storage.
     * Al igual que en las 'Fotos', se prefiere el alojamiento en la nube
     * para no depender del almacenamiento volátil del hosting (Render).
     */
    private String imagenUrl;
    
    /**
     * Texto opcional superpuesto en la historia, muy típico para dar contexto.
     */
    private String texto;

    /**
     * Fecha y hora exacta de la creación de la historia.
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha y hora exacta en la que la historia debe dejar de ser visible.
     */
    private LocalDateTime fechaExpiracion;

    /**
     * Evento del ciclo de vida de JPA (@PrePersist).
     * Este método se ejecuta automáticamente justo ANTES de guardar la historia en la base de datos por primera vez.
     * Es ideal para la lógica de negocio de las "stories", ya que configura de forma
     * determinista y transparente la creación actual y calcula exactamente que
     * la expiración será dentro de 24 horas, sin que el desarrollador deba recordarlo.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaExpiracion = fechaCreacion.plusHours(24);
    }
}
