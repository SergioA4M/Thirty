package thirty_api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author; // Por ahora usaremos el nombre, luego lo enlazaremos al User ID

    @Column(length = 500)
    private String content;

    private LocalDateTime createdAt;

    // Constructor vacío para JPA
    public Post() {}

    // Getters y Setters
    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}