package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad 'Comentario': Permite a los usuarios interactuar escribiendo 
 * mensajes en el perfil de otros usuarios (lo que antiguamente era el "Muro" de Tuenti).
 * Fomenta la comunicación y visibilidad de las relaciones.
 */
@Entity // Mapea esta clase a la tabla 'comentario' de PostgreSQL.
@Data // Lombok genera los getters, setters, constructores y métodos toString automáticamente.
public class Comentario {

    /**
     * Identificador único auto-incremental del comentario en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Contenido textual del mensaje dejado en el muro del perfil.
     * Al utilizar columnDefinition = "TEXT", permitimos que los comentarios 
     * no tengan el límite restrictivo de 255 caracteres por defecto, 
     * lo cual era común para felicitaciones largas de cumpleaños, etc.
     */
    @Column(columnDefinition = "TEXT")
    private String contenido;

    /**
     * Fecha y hora exacta de la publicación del comentario.
     * Se inicializa por defecto al momento de crearlo (LocalDateTime.now()).
     */
    private LocalDateTime fecha = LocalDateTime.now();

    /**
     * Relación Mucho a Uno: Muchos comentarios pueden haber sido escritos por un solo usuario.
     * Identifica "quién" escribió el comentario (el remitente).
     * En la BD será una clave foránea (autor_id) apuntando a la tabla de Usuarios.
     */
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private User autor;

    /**
     * Relación Mucho a Uno: Muchos comentarios pueden pertenecer al muro de un solo usuario.
     * Identifica "en el perfil de quién" se dejó el comentario (el destinatario).
     * En la BD será otra clave foránea (perfil_id) apuntando a la tabla de Usuarios.
     */
    @ManyToOne
    @JoinColumn(name = "perfil_id")
    private User perfil;

    /**
     * Bandera para el sistema de notificaciones.
     * Cuando se crea un comentario en el perfil de otra persona, 'leido' empieza en 'false'.
     * Cuando esa persona revisa sus notificaciones, pasa a 'true', actualizando la bolita roja en la UI.
     */
    private boolean leido = false;
}
