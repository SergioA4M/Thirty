package thirty_api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty; // Nueva importación necesaria
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    // Cambiado: Ahora permite registrarse (escribir) pero protege la salida (leer)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    // --- CAMPOS ESTILO TUENTI ---
    private String bio;           // Estado actual (Burbuja de texto)
    private String fotoPerfil;
    private String estudios;      // Ej: "Licenciatura en Antropología"
    private String trayectoria;   // Ej: "Abogados Sauceda"
    private String colegios;      // Ej: "San José de Calasanz"
    private String zonasMarcha;   // Ej: "Ocho y medio, Madrid"
    // ----------------------------

    @ManyToMany
    @JoinTable(
            name = "amistades",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "amigo_id")
    )
    @JsonIgnore // Mantenemos este para evitar el bucle infinito de amigos
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> amigos = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    
    private boolean online = false;
    private LocalDateTime lastSeen;
    
    @Column(columnDefinition = "TEXT")
    private String espacioPersonal;
}