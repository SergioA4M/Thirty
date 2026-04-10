package thirty_api.controllers; // Corregido el 'package'

import thirty_api.models.NotificacionesDTO;
import thirty_api.models.User;
import thirty_api.repositories.ComentarioRepository;
import thirty_api.repositories.FotoRepository;
import thirty_api.repositories.MensajeRepository;
import thirty_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        User user = userRepository.findByEmail(loginData.getEmail());
        if (user != null && user.getPasswordHash().equals(loginData.getPasswordHash())) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("Email o contraseña incorrectos");
        }
    }

    @PostMapping("/{id}/upload-foto")
    public ResponseEntity<String> uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("Archivo vacío");

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("Usuario no encontrado");

            User user = userOpt.get();
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = "foto_" + id + extension;

            Path ruta = Paths.get("uploads").resolve(nombreArchivo);
            Files.copy(file.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

            user.setFotoPerfil(nombreArchivo);
            userRepository.save(user);

            return ResponseEntity.ok(nombreArchivo);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al guardar la imagen: " + e.getMessage());
        }
    }

    @GetMapping
    public List<User> obtenerTodos() {
        return userRepository.findAll();
    }

    @PostMapping("/{id}/agregar/{amigoId}")
    public ResponseEntity<?> agregarAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User amigo = userRepository.findById(amigoId).orElseThrow(() -> new RuntimeException("Amigo no encontrado"));

        usuario.getAmigos().add(amigo);
        amigo.getAmigos().add(usuario);

        userRepository.save(usuario);
        userRepository.save(amigo);

        return ResponseEntity.ok("Conexión establecida");
    }

    @GetMapping("/{id}/amigos")
    public ResponseEntity<java.util.Set<User>> obtenerListaAmigos(@PathVariable Long id) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(usuario.getAmigos());
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<User> actualizarPerfil(@PathVariable Long id, @RequestBody User datos) {
        return userRepository.findById(id).map(user -> {
            user.setBio(datos.getBio());
            user.setEstudios(datos.getEstudios());
            user.setTrayectoria(datos.getTrayectoria());
            user.setColegios(datos.getColegios());
            user.setZonasMarcha(datos.getZonasMarcha());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerUsuario(@PathVariable Long id) {
        return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // --- NOTIFICACIONES CORREGIDAS ---
    @GetMapping("/notificaciones/resumen/{id}")
    public ResponseEntity<NotificacionesDTO> getNotificaciones(@PathVariable Long id) {
        // 1. Mensajes no leídos
        long m = mensajeRepository.countByReceptorIdAndLeidoFalse(id);

        // 2. Comentarios no leídos (Usamos el método que creamos en el Repository)
        long c = comentarioRepository.countByPerfilIdAndLeidoFalse(id);

        // 3. Fotos
        long f = 0;
        try {
            f = fotoRepository.findByUsuarioId(id).size();
        } catch (Exception e) {
            f = 0;
        }

        return ResponseEntity.ok(new NotificacionesDTO(m, c, f));
    }
}


