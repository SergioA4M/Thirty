package thirty_api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad 'User': Representa a un usuario dentro de la red social.
 * 
 * En Spring Boot y JPA (Java Persistence API), utilizamos anotaciones para mapear 
 * nuestras clases de Java directamente a tablas en la base de datos PostgreSQL.
 * Esta clase es el núcleo de nuestra aplicación, ya que todas las interacciones 
 * (fotos, posts, comentarios) giran en torno al usuario.
 */
@Entity // Indica a JPA que esta clase es una entidad que se guardará en la base de datos.
@Table(name = "users") // Especifica el nombre de la tabla en PostgreSQL (usamos 'users' en plural).
@Data // Anotación de Lombok: Genera automáticamente getters, setters, toString, equals y hashCode, ahorrando mucho código repetitivo.
public class User {

    /**
     * Identificador único del usuario (Clave Primaria).
     * @Id indica que es la clave primaria.
     * @GeneratedValue(strategy = GenerationType.IDENTITY) delega a la base de datos 
     * la responsabilidad de generar este número automáticamente (autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del usuario.
     * @Column(nullable = false) indica que este campo es obligatorio en la base de datos (NOT NULL).
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * Apellidos del usuario. Puede ser nulo si el usuario no lo proporciona.
     */
    private String lastName;

    /**
     * Correo electrónico del usuario (se usa para iniciar sesión).
     * unique = true asegura que no haya dos usuarios con el mismo email registrados.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Contraseña encriptada del usuario.
     * @JsonProperty(access = JsonProperty.Access.WRITE_ONLY): Esta es una medida de seguridad MUY IMPORTANTE.
     * Permite que el cliente (frontend) envíe la contraseña al registrarse (WRITE), 
     * pero evita que el servidor devuelva la contraseña (incluso encriptada) cuando 
     * se consulta información del usuario (evita fugas de datos al serializar a JSON).
     */
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    // --- CAMPOS ESTILO TUENTI (Nostalgia) ---
    // Estos campos capturan la esencia de las redes sociales clásicas.
    
    /** Estado actual del usuario (la típica "burbuja de texto" de Tuenti). */
    private String bio;           
    
    /** URL de la foto de perfil almacenada (normalmente en Supabase Storage). */
    private String fotoPerfil;
    
    /** Información académica del usuario. */
    private String estudios;      
    
    /** Información laboral o profesional del usuario. */
    private String trayectoria;   
    
    /** Colegios o institutos donde ha estudiado (muy típico para buscar viejos amigos). */
    private String colegios;      
    
    /** Zonas por las que el usuario suele salir de fiesta. */
    private String zonasMarcha;   
    // ----------------------------------------

    /**
     * Relación Muchos a Muchos: Un usuario puede tener muchos amigos, 
     * y esos amigos pueden ser amigos de muchos usuarios.
     * 
     * @ManyToMany indica la relación en base de datos.
     * @JoinTable crea una tabla intermedia llamada "amistades" para gestionar las relaciones,
     * relacionando el 'usuario_id' con el 'amigo_id'.
     * 
     * @JsonIgnore: Evita bucles infinitos al convertir el usuario a JSON (Ej: Usuario A carga Amigo B, 
     * que a su vez carga a Usuario A, etc.).
     * @ToString.Exclude y @EqualsAndHashCode.Exclude: Evitan errores de desbordamiento de memoria (StackOverflow)
     * causados por dependencias circulares en los métodos generados por Lombok.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "amistades",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "amigo_id")
    )
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> amigos = new HashSet<>();

    /**
     * Fecha y hora en la que el usuario se registró en la plataforma.
     * Se inicializa automáticamente al momento exacto de crear el objeto.
     */
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Estado de conexión en tiempo real (útil para el chat).
     */
    private boolean online = false;
    
    /**
     * Última vez que el usuario fue visto activo en la plataforma.
     */
    private LocalDateTime lastSeen;
    
    /**
     * Un texto libre donde el usuario puede expresarse, similar a los antiguos tablones o descripciones largas.
     * columnDefinition = "TEXT" permite almacenar cadenas mucho más largas que el VARCHAR(255) estándar.
     */
    @Column(columnDefinition = "TEXT")
    private String espacioPersonal;
}