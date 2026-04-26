package thirty_api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad 'Post': Representa una publicación en el "Tablón de Novedades" (Muro).
 * Es el núcleo de la interacción social en la plataforma, permitiendo a los usuarios
 * compartir estados, reflexiones o enlaces con su red de amigos.
 */
@Entity // Indica a JPA que esta clase se mapeará a una tabla (por defecto llamada 'post') en la base de datos PostgreSQL.
public class Post {
    
    /**
     * Identificador único de cada publicación.
     * @Id y @GeneratedValue(strategy = GenerationType.IDENTITY) delegan la creación
     * de este ID auto-incremental a la base de datos de manera secuencial.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del autor que realizó la publicación.
     * En etapas iniciales se almacenaba el nombre directamente (String).
     * Nota para el TFG: En una arquitectura más avanzada, esto podría ser una relación @ManyToOne hacia la entidad User.
     */
    private String author;

    /**
     * El contenido de texto de la publicación.
     * @Column(length = 500): Limita el tamaño del texto en la base de datos a 500 caracteres,
     * fomentando mensajes más directos al estilo clásico de las redes sociales.
     */
    @Column(length = 500)
    private String content;

    /**
     * Fecha y hora exacta en la que se creó el post.
     * Fundamental para ordenar el Tablón de Novedades de forma cronológica (del más reciente al más antiguo).
     */
    private LocalDateTime createdAt;

    /**
     * Constructor vacío: Obligatorio para que el framework JPA (Hibernate) 
     * pueda instanciar la clase al recuperar los datos de la base de datos utilizando reflexión (reflection).
     */
    public Post() {}

    // Getters y Setters tradicionales (en otras entidades usamos Lombok @Data para evitar este código repetitivo)
    public Long getId() { return id; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}