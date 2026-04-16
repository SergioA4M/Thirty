package thirty_api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thirty_api.models.Foto;
import thirty_api.models.User;
import thirty_api.repositories.FotoRepository;
import thirty_api.repositories.UserRepository;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * Controlador de Fotos (FotoController).
 *
 * ¿Qué es esto?
 * En Spring Boot, un @RestController es una clase que maneja las peticiones que
 * llegan desde el navegador o la app (Frontend) a través de Internet (URLs).
 * Este controlador en particular se encarga de todo lo relacionado con el álbum
 * de fotos de los usuarios: subir nuevas fotos, ver las fotos de alguien o borrarlas.
 */
@RestController // Indica que esta clase responderá con datos (normalmente JSON), no con páginas HTML directamente.
@RequestMapping("/api/fotos") // Todas las URLs de este controlador empezarán por "/api/fotos"
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite que cualquier página web pueda pedirle datos a esta API (Cross-Origin Resource Sharing)
public class FotoController {

    // @Autowired le dice a Spring: "Por favor, dame el componente que maneja la base de datos de las fotos".
    // Así no tenemos que crearlo manualmente, Spring nos lo "inyecta".
    @Autowired private FotoRepository fotoRepository;
    
    // Repositorio para acceder a los datos de los usuarios.
    @Autowired private UserRepository userRepository;
    
    // Servicio que creamos para conectarnos con la nube (Supabase) y guardar los archivos allí.
    @Autowired private thirty_api.services.SupabaseService supabaseService;

    /**
     * Endpoint para subir una foto nueva al álbum de un usuario.
     * @PostMapping significa que esta función se ejecuta cuando el Frontend envía datos mediante el método POST (para crear).
     * URL: POST /api/fotos/upload/{userId}
     */
    @PostMapping("/upload/{userId}")
    public ResponseEntity<?> subirFoto(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            // 1. Buscamos al usuario en la base de datos usando el ID que viene en la URL (@PathVariable).
            User user = userRepository.findById(userId).orElse(null);
            
            // Si el usuario no existe, devolvemos un error 404 (No encontrado).
            if (user == null) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            // 2. Generamos un nombre único para el archivo.
            // Usamos UUID.randomUUID() para evitar que dos fotos con el mismo nombre se sobrescriban.
            String nombreArchivo = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // 3. Subir el archivo físicamente a la nube (Supabase Storage).
            // Le pasamos los bytes (el contenido de la imagen), el nombre que generamos y el tipo (ej: image/jpeg).
            supabaseService.uploadFile(file.getBytes(), nombreArchivo, file.getContentType());

            // 4. Guardar el registro en la base de datos PostgreSQL.
            Foto foto = new Foto();
            foto.setUrl(nombreArchivo); // Guardamos el nombre único, luego la URL completa se forma dinámicamente o por redirección
            foto.setUsuario(user);      // Asignamos de quién es la foto
            
            // Guardamos la foto en la base de datos y devolvemos la información guardada al Frontend (código HTTP 200 OK).
            return ResponseEntity.ok(fotoRepository.save(foto));

        } catch (Exception e) {
            // Si algo falla, lo imprimimos en la consola del servidor para poder depurarlo.
            e.printStackTrace();
            // Y le avisamos al Frontend que hubo un Error Interno del Servidor (código 500).
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para obtener todas las fotos del álbum de un usuario concreto.
     * @GetMapping responde a peticiones GET (para leer o pedir datos).
     * URL: GET /api/fotos/usuario/{userId}
     */
    @GetMapping("/usuario/{userId}")
    public List<Foto> getFotos(@PathVariable Long userId) {
        // Le pedimos al repositorio que busque en la base de datos todas las fotos
        // que pertenezcan al usuario con el ID especificado, y devolvemos la lista.
        return fotoRepository.findByUsuarioId(userId);
    }
    
    /**
     * Endpoint para borrar una foto del sistema.
     * @DeleteMapping responde a peticiones DELETE.
     * URL: DELETE /api/fotos/{fotoId}
     */
    @DeleteMapping("/{fotoId}")
    public ResponseEntity<?> borrarFoto(@PathVariable Long fotoId) {
        try {
            // 1. Buscamos el registro de la foto en la base de datos.
            Foto foto = fotoRepository.findById(fotoId).orElseThrow();

            // 2. Borrar el archivo físico de Supabase para no ocupar espacio basura
            supabaseService.deleteFile(foto.getUrl()); 

            // 3. Borrar el registro de la base de datos.
            fotoRepository.delete(foto);

            // Devolvemos un OK (200) vacío, indicando que el borrado fue exitoso.
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Si hay algún problema (ej: la foto no existe), devolvemos un error 500.
            return ResponseEntity.status(500).body("Error al borrar la foto: " + e.getMessage());
        }
    }
}
