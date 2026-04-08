package thirty_api.controllers;

import thirty_api.models.User;
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
@CrossOrigin(origins = "*") // IMPORTANTE para que el JS de tu HTML pueda conectar
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // HE BORRADO getAllUsers() PORQUE obtenerTodos() HACE LO MISMO ABAJO

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userRepository.save(user);
    }
    // --- MÉTODO DE LOGIN (AÑADE ESTO) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        // 1. Buscamos al usuario por el email
        User user = userRepository.findByEmail(loginData.getEmail());

        // 2. Comprobamos si existe y si la contraseña coincide
        if (user != null && user.getPasswordHash().equals(loginData.getPasswordHash())) {
            // Si es correcto, devolvemos el usuario para que el JS guarde su ID
            return ResponseEntity.ok(user);
        } else {
            // Si falla, devolvemos error 401 (No autorizado)
            return ResponseEntity.status(401).body("Email o contraseña incorrectos");
        }
    }

    // --- MÉTODO PARA LA FOTO DE PERFIL ---
    @PostMapping("/{id}/upload-foto")
    public ResponseEntity<String> uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Archivo vacío");
        }

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }
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


    // 1. Obtener TODOS los usuarios (para la sección "Gente")
    @GetMapping
    public List<User> obtenerTodos() {
        return userRepository.findAll();
    }

    // 2. Lógica para agregar un amigo
    @PostMapping("/{id}/agregar/{amigoId}")
    public ResponseEntity<?> agregarAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User amigo = userRepository.findById(amigoId)
                .orElseThrow(() -> new RuntimeException("Amigo no encontrado"));

        // Añadimos la relación (esto escribe en la tabla 'amistades')
        usuario.getAmigos().add(amigo);

        // Hacemos que sea mutuo para que ambos se vean en el chat
        amigo.getAmigos().add(usuario);

        userRepository.save(usuario);
        userRepository.save(amigo);

        return ResponseEntity.ok("Conexión establecida");
    }
    @GetMapping("/{id}/amigos")
    public ResponseEntity<java.util.Set<User>> obtenerListaAmigos(@PathVariable Long id) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(usuario.getAmigos());
    }
    @PutMapping("/{id}/perfil")
    public ResponseEntity<User> actualizarPerfil(@PathVariable Long id, @RequestBody User datos) {
        return userRepository.findById(id).map(user -> {
            // Actualizamos los campos nuevos estilo Tuenti
            user.setBio(datos.getBio());
            user.setEstudios(datos.getEstudios());
            user.setTrayectoria(datos.getTrayectoria());
            user.setColegios(datos.getColegios());
            user.setZonasMarcha(datos.getZonasMarcha());

            User guardado = userRepository.save(user);
            return ResponseEntity.ok(guardado);
        }).orElse(ResponseEntity.notFound().build());
    }
    // 3. Obtener UN SOLO usuario por ID (Fundamental para cargar el perfil)
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerUsuario(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}