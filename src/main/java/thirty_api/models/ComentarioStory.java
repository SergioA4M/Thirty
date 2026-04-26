package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad 'ComentarioStory': Una tabla auxiliar diseñada exclusivamente para 
 * almacenar las respuestas directas que hacen los usuarios sobre las "Stories" 
 * de otros usuarios, diferenciándose de los comentarios en el tablón (Muro).
 */
@Entity
@Data
@Table(name = "comentarios_stories") // Fundamental especificar el nombre ya que por defecto JPA usaría "comentario_story".
public class ComentarioStory {

    /**
     * ID autoincremental de la respuesta (Clave primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación Mucho a Uno: Muchos comentarios pueden pertenecer a una misma Historia efímera.
     * En la BD será una clave foránea 'story_id' apuntando a la tabla 'stories'.
     */
    @ManyToOne
    @JoinColumn(name = "story_id")
    private Story story;

    /**
     * Relación Mucho a Uno: Muchos comentarios a historias pueden ser escritos por un mismo usuario.
     * En la BD será una clave foránea 'usuario_id' apuntando a la tabla 'users'.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /**
     * El texto de la respuesta a la historia.
     * TEXT se utiliza porque los emojis, stickers (en formato texto) o URLs 
     * pueden exceder la longitud de un string normal de base de datos.
     */
    @Column(columnDefinition = "TEXT")
    private String contenido;

    /**
     * Fecha y hora en la que el usuario envió su reacción/respuesta a la historia.
     */
    private LocalDateTime fecha;

    /**
     * Ciclo de vida JPA: Antes de insertar en la base de datos, 
     * nos aseguramos de asignar el momento temporal exacto sin 
     * depender de que el controlador lo asigne manualmente.
     */
    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}
