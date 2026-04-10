package thirty_api.models;

public class NotificacionesDTO {
    private long mensajes;
    private long comentarios;
    private long fotos;

    public NotificacionesDTO(long mensajes, long comentarios, long fotos) {
        this.mensajes = mensajes;
        this.comentarios = comentarios;
        this.fotos = fotos;
    }

    // Getters para que Spring pueda enviarlos a la web
    public long getMensajes() { return mensajes; }
    public long getComentarios() { return comentarios; }
    public long getFotos() { return fotos; }
}
