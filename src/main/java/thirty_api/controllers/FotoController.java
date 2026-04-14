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

@RestController
@RequestMapping("/api/fotos")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class FotoController {

    @Autowired private FotoRepository fotoRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/upload/{userId}")
    public ResponseEntity<?> subirFoto(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }

            String nombreArchivo = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path ruta = uploadsDir.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

            Foto foto = new Foto();
            foto.setUrl(nombreArchivo);
            foto.setUsuario(user);
            return ResponseEntity.ok(fotoRepository.save(foto));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/usuario/{userId}")
    public List<Foto> getFotos(@PathVariable Long userId) {
        return fotoRepository.findByUsuarioId(userId);
    }
    @DeleteMapping("/{fotoId}")
    public ResponseEntity<?> borrarFoto(@PathVariable Long fotoId) {
        try {
            Foto foto = fotoRepository.findById(fotoId).orElseThrow();

            // 1. Borrar el archivo físico de la carpeta 'uploads'
            Path rutaArchivo = Paths.get("uploads").resolve(foto.getUrl());
            Files.deleteIfExists(rutaArchivo);

            // 2. Borrar de la base de datos
            fotoRepository.delete(foto);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al borrar la foto: " + e.getMessage());
        }
    }
}
