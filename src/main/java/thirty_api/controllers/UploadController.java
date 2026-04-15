package thirty_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controlador puente para imagenes antiguas (o si mantienes las urls /uploads/...).
 * Cuando el frontend pida "/uploads/foto.png", este controlador la buscar\u00e1 en tu Supabase p\u00fablico autom\u00e1ticamente.
 */
@RestController
@RequestMapping("/uploads")
public class UploadController {

    // Cambia esto por tu URL real de Supabase
    private final String SUPABASE_URL = "https://ylxpcytucebvzijqyrsx.supabase.co";

    /**
     * Recibe la petici\u00f3n de /uploads/foto.png y redirecciona a la URL de Supabase.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Void> getFileFromSupabase(@PathVariable String filename) {
        // La URL de Supabase P\u00fablica para descargar/ver el archivo de tu bucket "uploads"
        String supabasePublicUrl = SUPABASE_URL + "/storage/v1/object/public/uploads/" + filename;
        
        // 302 FOUND -> Le dice al navegador "Ve a buscar la foto a esta direcci\u00f3n de Supabase"
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(supabasePublicUrl))
                .build();
    }
}