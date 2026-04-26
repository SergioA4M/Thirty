package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad 'Mensaje': Constituye el sistema de chat privado entre usuarios.
 * Aunque Thirty se basa mucho en interacciones públicas (Muros, Posts),
 * la mensajería privada 1 a 1 es indispensable.
 * 
 * Implementación clásica donde un usuario es el emisor y otro el receptor,
 * almacenando texto que incluso puede incluir enlaces o emojis de forma nativa.
 */
@Entity
@Data
@Table(name = "mensajes") // Nomenclatura plural explícita para PostgreSQL.
public class Mensaje {

    /**
     * Identificador único del mensaje en la base de datos (Clave Primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación Mucho a Uno: Muchos mensajes pueden ser enviados por un mismo usuario.
     * Identifica quién originó el mensaje.
     * En BD será una Foreign Key "emisor_id" hacia la tabla "users".
     */
    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private User emisor;

    /**
     * Relación Mucho a Uno: Muchos mensajes pueden ser recibidos por un mismo usuario.
     * Identifica el destinatario.
     * En BD será una Foreign Key "receptor_id" hacia la tabla "users".
     */
    @ManyToOne
    @JoinColumn(name = "receptor_id")
    private User receptor;

    /**
     * El cuerpo del mensaje.
     * Se utiliza 'TEXT' porque los chats suelen exceder el límite de 255 caracteres 
     * típicos de un VARCHAR convencional y para permitir caracteres especiales, URLs largas, etc.
     */
    @Column(columnDefinition = "TEXT")
    private String contenido;

    /**
     * Marca de tiempo (Timestamp) exacta en la que se envió el mensaje.
     * Fundamental para poder ordenar las conversaciones de chat de forma cronológica.
     */
    private LocalDateTime fechaEnvio;

    /**
     * Booleano para el sistema de acuse de recibo ("Check" y "Doble Check Azul").
     * Cuando se crea, está en 'false'. Cuando el receptor entra a la conversación de chat,
     * el backend debe actualizar esto a 'true'.
     */
    private boolean leido = false;

    /**
     * Constructor que asigna automáticamente la fecha y hora de envío actual
     * en el instante en que el objeto mensaje se instancia en la memoria de Java.
     */
    public Mensaje() {
        this.fechaEnvio = LocalDateTime.now();
    }
}
