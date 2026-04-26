package thirty_api.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad 'Foto': Representa una imagen subida por un usuario a su álbum personal.
 * En el contexto de "Thirty", las fotos son esenciales para revivir la época 
 * de las cámaras digitales y los álbumes compartidos de Tuenti.
 * 
 * Originalmente, estas imágenes se guardaban localmente (carpeta /uploads/). Sin embargo,
 * en servicios de alojamiento efímero como Render, los archivos locales se eliminan
 * con cada nuevo despliegue. Por esta razón, el backend ahora sube y sirve 
 * las imágenes permanentemente desde un 'bucket' de Supabase Storage.
 */
@Entity // Indica a JPA que esta clase es una tabla en la base de datos (se creará la tabla 'foto' por defecto).
@Data // Anotación de Lombok: Genera getters, setters, toString, equals y hashCode, manteniendo el código limpio.
public class Foto {

    /**
     * Identificador único de la foto (Clave Primaria en la base de datos).
     * @Id y @GeneratedValue indican que es autoincremental, manejado por PostgreSQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * URL completa de la imagen o nombre del archivo.
     * En la implementación actual, este campo almacena el nombre único del archivo (ej: 171829_mifoto.jpg)
     * que luego se utiliza para construir la URL pública hacia el bucket de Supabase (ej: https://[PROYECTO_ID].supabase.co/storage/v1/object/public/uploads/171829_mifoto.jpg).
     */
    private String url;

    /**
     * Breve descripción o título que el usuario le da a su foto.
     */
    private String descripcion;

    /**
     * Relación Muchos a Uno: Muchas fotos pueden pertenecer a un mismo Usuario.
     * @ManyToOne: Especifica la relación con la clase 'User'.
     * @JoinColumn: Indica que en la tabla 'foto', habrá una columna llamada 'user_id' 
     * que actuará como Clave Foránea (Foreign Key) apuntando al id de la tabla 'users'.
     * Es decir, establece "quién es el dueño" de esta foto en particular.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario; // El dueño del álbum que subió la foto
}
