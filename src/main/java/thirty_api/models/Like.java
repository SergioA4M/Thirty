package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad 'Like' o "Me gusta": Funcionalidad universal de feedback rápido
 * de toda red social. Registra y cuantifica las reacciones de los usuarios 
 * frente al contenido generado por otros (posts del tablón, historias, etc.).
 * 
 * Este diseño utiliza el patrón de persistencia polimórfico,
 * lo que evita tener que crear múltiples tablas distintas de "Like" 
 * para cada tipo de entidad que se puede puntuar.
 */
@Entity
@Data // Anotación de Lombok: Reduce el código que escribe el desarrollador delegando a la biblioteca la generación de constructores y accessors.
@Table(name = "likes") // "like" es una palabra reservada en SQL, por lo que es obligatorio mapearlo como "likes".
public class Like {

    /**
     * Identificador autoincremental en PostgreSQL (Clave primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación Mucho a Uno: Muchos "Likes" pueden ser realizados por un mismo Usuario.
     * En la BD se almacena una clave foránea apuntando al id de usuario de quien "da" el Like.
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /**
     * Define a qué elemento de la red social pertenece este Like.
     * Almacenará valores como "post", "comentario" o "story".
     * Esta técnica, conocida como relación polimórfica (o diseño Entity-Attribute-Value en su forma ligera), 
     * nos ahorra crear clases LikePost, LikeComentario, LikeStory.
     */
    private String tipo;
    
    /**
     * El ID numérico exacto de la entidad (un ID de post, de comentario, o de historia),
     * dependiendo de lo que se especificó en el atributo 'tipo'.
     */
    private Long entidadId;

    /**
     * Fecha y hora en la que el usuario dio al botón de "Me gusta".
     */
    private LocalDateTime fecha;

    /**
     * Ciclo de vida de JPA: Se ejecuta instantes antes de que los datos 
     * se guarden en la base de datos por primera vez para asegurar 
     * una precisión exacta del momento sin depender del frontend.
     */
    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}
