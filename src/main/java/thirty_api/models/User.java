package thirty_api.models;

import com.fasterxml.jackson.annotation.JsonIgnore; // IMPORTANTE: Para evitar el bucle infinito en JSON
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

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String bio;
    private String fotoPerfil;

    @ManyToMany
    @JoinTable(
            name = "amistades",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "amigo_id")
    )
    @JsonIgnore // <--- Esto evita el error "Document nesting depth (501)"
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> amigos = new HashSet<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}