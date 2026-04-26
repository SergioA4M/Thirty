package thirty_api.models;

/**
 * Clase 'NotificacionesDTO' (Data Transfer Object).
 * IMPORTANTE: ¡Esto NO es una Entidad (@Entity)! No se guarda en la base de datos.
 * 
 * Un DTO es un "Objeto de Transferencia de Datos". Se usa exclusivamente para empaquetar 
 * información de varias fuentes (consultas a base de datos) y enviarla de forma 
 * estructurada y limpia al frontend (como un JSON) en una sola respuesta HTTP.
 * 
 * En Thirty, este DTO resume cuántos mensajes, comentarios y alertas de fotos (como likes) 
 * NO LEÍDOS tiene un usuario, para pintar los "globitos rojos" (badges) en el menú principal.
 */
public class NotificacionesDTO {
    
    /**
     * Cantidad de mensajes privados no leídos.
     */
    private long mensajes;
    
    /**
     * Cantidad de comentarios en el muro no leídos.
     */
    private long comentarios;
    
    /**
     * Cantidad de interacciones (likes, etiquetas) en fotos/stories no leídas.
     */
    private long fotos;

    /**
     * Constructor del DTO que el controlador instanciará con el resultado 
     * de los 'COUNT' de los distintos repositorios JPA.
     */
    public NotificacionesDTO(long mensajes, long comentarios, long fotos) {
        this.mensajes = mensajes;
        this.comentarios = comentarios;
        this.fotos = fotos;
    }

    // Getters obligatorios: Spring Boot utiliza la librería Jackson por debajo, 
    // y Jackson necesita métodos "getAlgo()" para poder convertir este objeto a formato JSON 
    // (Ej: {"mensajes": 2, "comentarios": 1, "fotos": 0}) y mandarlo a la web.
    public long getMensajes() { return mensajes; }
    public long getComentarios() { return comentarios; }
    public long getFotos() { return fotos; }
}
