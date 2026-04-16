package thirty_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Controlador de Subidas / Archivos (UploadController).
 * Este controlador redirige cualquier petición de `/uploads/archivo.png`
 * directamente a la URL pública de tu bucket de Supabase.
 */
@RestController 
@RequestMapping("/uploads")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class UploadController {

    private final String SUPABASE_URL = "https://ylxpcytucebvzijqyrsx.supabase.co";

    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        System.out.println("=== Redirigiendo foto hacia Supabase: " + filename + " ===");
        
        // Construimos la URL pública del archivo en Supabase
        String supabasePublicUrl = SUPABASE_URL + "/storage/v1/object/public/uploads/" + filename;
        
        // Devolvemos un código 302 para que el navegador vaya a buscar la imagen a Supabase
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(supabasePublicUrl))
                .build();
    }
}