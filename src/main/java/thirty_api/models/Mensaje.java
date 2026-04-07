package thirty_api.models; // Importante que coincida con tu carpeta

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String emisor;
    private String receptor;
    @Column(length = 1000)
    private String contenido;
    private LocalDateTime fecha = LocalDateTime.now();

    public Mensaje() {}

    // Getters y Setters
    public Long getId() { return id; }
    public String getEmisor() { return emisor; }
    public void setEmisor(String emisor) { this.emisor = emisor; }
    public String getReceptor() { return receptor; }
    public void setReceptor(String receptor) { this.receptor = receptor; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDateTime getFecha() { return fecha; }
}
