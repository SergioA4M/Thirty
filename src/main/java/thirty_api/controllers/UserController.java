package thirty_api.controllers;

import thirty_api.models.NotificacionesDTO;
import thirty_api.models.User;
import thirty_api.models.Notificacion;
import thirty_api.repositories.ComentarioRepository;
import thirty_api.repositories.FotoRepository;
import thirty_api.repositories.MensajeRepository;
import thirty_api.repositories.UserRepository;
import thirty_api.repositories.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private thirty_api.repositories.NotificacionRepository notificacionRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre es obligatorio");
            }
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es obligatorio");
            }
            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña es obligatoria");
            }
            user.setCreatedAt(LocalDateTime.now());
            return ResponseEntity.ok(userRepository.save(user));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al registrar: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        User user = userRepository.findByEmail(loginData.getEmail());
        if (user != null && user.getPasswordHash().equals(loginData.getPasswordHash())) {
            user.setOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
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
            if (datos.getBio() != null) user.setBio(datos.getBio());
            if (datos.getEstudios() != null) user.setEstudios(datos.getEstudios());
            if (datos.getTrayectoria() != null) user.setTrayectoria(datos.getTrayectoria());
            if (datos.getColegios() != null) user.setColegios(datos.getColegios());
            if (datos.getZonasMarcha() != null) user.setZonasMarcha(datos.getZonasMarcha());
            if (datos.getEspacioPersonal() != null) user.setEspacioPersonal(datos.getEspacioPersonal());
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

    @GetMapping("/buscar")
    public ResponseEntity<List<User>> buscarUsuarios(@RequestParam String q) {
        List<User> todos = userRepository.findAll();
        String query = q.toLowerCase();
        List<User> resultados = todos.stream()
            .filter(u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(query)
                      || u.getEmail().toLowerCase().contains(query))
            .toList();
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/online/{id}")
    public ResponseEntity<Map<String, Object>> estadoOnline(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        Map<String, Object> estado = new HashMap<>();
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            estado.put("online", u.isOnline());
            estado.put("lastSeen", u.getLastSeen());
        } else {
            estado.put("online", false);
        }
        return ResponseEntity.ok(estado);
    }

    @PostMapping("/set-offline/{id}")
    public ResponseEntity<?> setOffline(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setOnline(false);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/set-online/{id}")
    public ResponseEntity<?> setOnline(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/amigos/estado/{id}")
    public ResponseEntity<List<Map<String, Object>>> amigosConEstado(@PathVariable Long id) {
        User usuario = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Map<String, Object>> amigos = usuario.getAmigos().stream().map(amigo -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", amigo.getId());
            info.put("firstName", amigo.getFirstName());
            info.put("lastName", amigo.getLastName());
            info.put("fotoPerfil", amigo.getFotoPerfil());
            info.put("online", amigo.isOnline());
            info.put("lastSeen", amigo.getLastSeen());
            return info;
        }).toList();
        
        return ResponseEntity.ok(amigos);
    }

    @PostMapping("/{id}/solicitar-amistad/{amigoId}")
    public ResponseEntity<?> solicitarAmistad(@PathVariable Long id, @PathVariable Long amigoId) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User amigo = userRepository.findById(amigoId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getAmigos().contains(amigo)) {
            return ResponseEntity.badRequest().body("Ya son amigos");
        }

        Notificacion notif = new Notificacion();
        notif.setUsuario(amigo);
        notif.setEmisor(usuario);
        notif.setTipo("solicitud_amistad");
        notif.setContenido(usuario.getFirstName() + " quiere ser tu amigo");
        notif.setEntidadId(id);
        notificacionRepository.save(notif);

        return ResponseEntity.ok("Solicitud enviada");
    }

    @PostMapping("/{id}/aceptar-amistad/{amigoId}")
    public ResponseEntity<?> aceptarAmistad(@PathVariable Long id, @PathVariable Long amigoId) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User amigo = userRepository.findById(amigoId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.getAmigos().add(amigo);
        amigo.getAmigos().add(usuario);
        userRepository.save(usuario);
        userRepository.save(amigo);

        return ResponseEntity.ok("Amistad aceptada");
    }
}


