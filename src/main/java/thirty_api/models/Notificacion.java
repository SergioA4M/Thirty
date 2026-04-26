package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad 'Notificacion': Unifica y estandariza el centro de avisos para el usuario.
 * Desde un "Te han enviado un mensaje" hasta un "Alguien te ha dejado un comentario en tu tablón".
 * 
 * Este patrón es crucial para la experiencia de una red social y requiere de dos actores:
 * el emisor (el que realiza la acción, e.g. dar like) y el receptor/usuario (el notificado).
 */
@Entity
@Data
@Table(name = "notificaciones") // Nombramos la tabla explícitamente en plural en PostgreSQL.
public class Notificacion {

    /**
     * Identificador único autoincremental de cada notificación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación Mucho a Uno: Muchas notificaciones pueden pertenecer a un mismo Usuario.
     * Este es el "receptor" de la notificación (A quien le aparece la alerta en la campana).
     */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    /**
     * Relación Mucho a Uno: Muchas notificaciones pueden ser provocadas por un mismo emisor.
     * Este es el usuario que causó la notificación (El que dio Like o mandó solicitud de amistad).
     */
    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private User emisor;

    /**
     * Categoriza el tipo de notificación, necesario para que el frontend decida 
     * qué icono o texto mostrar en el menú desplegable.
     * Valores típicos: "mensaje", "solicitud_amistad", "comentario", "like", "story".
     */
    @Column(nullable = false)
    private String tipo;

    /**
     * Un pequeño resumen o texto descriptivo de la acción que se va a mostrar al usuario.
     * Se usa TEXT para evitar los límites estrictos de los campos VARCHAR por defecto.
     */
    @Column(columnDefinition = "TEXT")
    private String contenido;

    /**
     * ID de la entidad relacionada a la notificación (ID del post, del comentario, etc.).
     * Permite que el frontend cree un enlace dinámico para redirigir al usuario al 
     * origen del aviso.
     */
    private Long entidadId;

    /**
     * Booleano crítico para la UI: Determina si el punto rojo de notificación nueva debe mostrarse o no.
     */
    private boolean leido = false;

    /**
     * Momento exacto de creación del evento que originó la notificación.
     */
    private LocalDateTime fecha;

    /**
     * Ciclo de vida de JPA: Se asigna la fecha y hora justos antes del persist 
     * (el momento en el que el objeto entra en contacto con la base de datos).
     */
    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}
