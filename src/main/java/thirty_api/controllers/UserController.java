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

/**
 * Controlador de Usuarios (UserController)
 * Maneja todas las peticiones relacionadas con los usuarios:
 * Registro, Login, Perfil, Subir fotos, y el sistema de Amigos.
 */
@RestController // Indica que esta clase es un controlador REST (devuelve JSON)
@RequestMapping("/api/users") // Todas las rutas de esta clase empezarán por /api/users
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite que el frontend se conecte sin dar error de CORS
public class UserController {

    // @Autowired inyecta automáticamente los repositorios (conexiones a la base de datos)
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

    /**
     * Registra un nuevo usuario en la base de datos.
     * Endpoint: POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        System.out.println("Intentando registrar: " + user.getEmail());
        
        // Validación básica: Comprobar que los campos obligatorios no estén vacíos
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre es obligatorio");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El email es obligatorio");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            return ResponseEntity.badRequest().body("La contraseña es obligatoria");
        }
        
        user.setCreatedAt(LocalDateTime.now()); // Fecha de creación del usuario
        User saved = userRepository.save(user); // Guarda el usuario en la BBDD
        System.out.println("Registrado OK, ID: " + saved.getId());
        return ResponseEntity.ok(saved); // Devuelve el usuario guardado con código 200 OK
    }

    /**
     * Inicia sesión comprobando el email y la contraseña.
     * Endpoint: POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        // Busca al usuario por su email
        User user = userRepository.findByEmail(loginData.getEmail());
        // Si el usuario existe y la contraseña coincide
        if (user != null && user.getPasswordHash().equals(loginData.getPasswordHash())) {
            user.setOnline(true); // Lo marcamos como "En línea"
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).body("Email o contraseña incorrectos");
        }
    }

    @Autowired
    private thirty_api.services.SupabaseService supabaseService;

    /**
     * Sube una foto de perfil y la asocia al usuario en la base de datos y la almacena en Supabase.
     * Endpoint: POST /api/users/{id}/upload-foto
     */
    @PostMapping("/{id}/upload-foto")
    public ResponseEntity<?> uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("Archivo vacío");

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("Usuario no encontrado");

            User user = userOpt.get();

            // Genera un nombre único para la foto usando la fecha actual (timestamp)
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = "foto_" + id + "_" + System.currentTimeMillis() + extension;

            // En lugar de guardar en disco, sube la foto a tu servidor de SUPABASE Storage
            supabaseService.uploadFile(file.getBytes(), nombreArchivo, file.getContentType());

            // Actualiza el perfil del usuario con el nombre de la foto (ya que UploadController hace el redirect)
            user.setFotoPerfil(nombreArchivo);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("url", nombreArchivo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Devuelve una lista con todos los usuarios registrados.
     * Endpoint: GET /api/users
     */
    @GetMapping
    public List<User> obtenerTodos() {
        return userRepository.findAll();
    }

    /**
     * Agrega a otro usuario como amigo. Crea una relación en la base de datos y envía notificación.
     * Endpoint: POST /api/users/{id}/agregar/{amigoId}
     */
    @PostMapping("/{id}/agregar/{amigoId}")
    public ResponseEntity<?> agregarAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User amigo = userRepository.findById(amigoId).orElseThrow(() -> new RuntimeException("Amigo no encontrado"));

        // Comprueba si ya son amigos para no duplicar
        if (usuario.getAmigos().contains(amigo)) {
            return ResponseEntity.badRequest().body("Ya son amigos");
        }

        // Relación bidireccional (A es amigo de B, y B es amigo de A)
        usuario.getAmigos().add(amigo);
        amigo.getAmigos().add(usuario);
        userRepository.save(usuario);
        userRepository.save(amigo);

        // Crea una notificación para el amigo avisándole de que ahora sois amigos
        Notificacion notif = new Notificacion();
        notif.setUsuario(amigo);
        notif.setEmisor(usuario);
        notif.setTipo("solicitud_amistad");
        notif.setContenido(usuario.getFirstName() + " y tú ahora son amigos");
        notif.setEntidadId(id);
        notificacionRepository.save(notif);

        return ResponseEntity.ok("Amigo añadido");
    }

    /**
     * Devuelve la lista de amigos de un usuario.
     * Endpoint: GET /api/users/{id}/amigos
     */
    @GetMapping("/{id}/amigos")
    public ResponseEntity<java.util.Set<User>> obtenerListaAmigos(@PathVariable Long id) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(usuario.getAmigos());
    }

    /**
     * Elimina a un usuario de la lista de amigos.
     * Endpoint: DELETE /api/users/{id}/amigos/{amigoId}
     */
    @DeleteMapping("/{id}/amigos/{amigoId}")
    public ResponseEntity<?> eliminarAmigo(@PathVariable Long id, @PathVariable Long amigoId) {
        User usuario = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        User amigo = userRepository.findById(amigoId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Elimina la relación en ambos sentidos
        usuario.getAmigos().remove(amigo);
        amigo.getAmigos().remove(usuario);
        userRepository.save(usuario);
        userRepository.save(amigo);

        return ResponseEntity.ok("Amigo eliminado");
    }

    /**
     * Actualiza los datos del perfil de un usuario (bio, estudios, etc.).
     * Endpoint: PUT /api/users/{id}/perfil
     */
    @PutMapping("/{id}/perfil")
    public ResponseEntity<User> actualizarPerfil(@PathVariable Long id, @RequestBody User datos) {
        return userRepository.findById(id).map(user -> {
            // Solo actualiza los campos que se han enviado (no nulos)
            if (datos.getBio() != null) user.setBio(datos.getBio());
            if (datos.getEstudios() != null) user.setEstudios(datos.getEstudios());
            if (datos.getTrayectoria() != null) user.setTrayectoria(datos.getTrayectoria());
            if (datos.getColegios() != null) user.setColegios(datos.getColegios());
            if (datos.getZonasMarcha() != null) user.setZonasMarcha(datos.getZonasMarcha());
            if (datos.getEspacioPersonal() != null) user.setEspacioPersonal(datos.getEspacioPersonal());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Devuelve la información pública de un usuario por su ID.
     * Endpoint: GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerUsuario(@PathVariable Long id) {
        return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un recuento rápido de notificaciones nuevas (mensajes, comentarios, etc.).
     * Endpoint: GET /api/users/notificaciones/resumen/{id}
     */
    @GetMapping("/notificaciones/resumen/{id}")
    public ResponseEntity<NotificacionesDTO> getNotificaciones(@PathVariable Long id) {
        long m = mensajeRepository.countByReceptorIdAndLeidoFalse(id); // Mensajes no leídos
        long c = comentarioRepository.countByPerfilIdAndLeidoFalse(id); // Comentarios no leídos

        long f = 0; // Contar fotos subidas
        try {
            f = fotoRepository.findByUsuarioId(id).size();
        } catch (Exception e) {
            f = 0;
        }

        return ResponseEntity.ok(new NotificacionesDTO(m, c, f));
    }

    /**
     * Buscador de usuarios por nombre, apellido o email.
     * Endpoint: GET /api/users/buscar?q=nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<User>> buscarUsuarios(@RequestParam String q) {
        List<User> todos = userRepository.findAll();
        String query = q.toLowerCase();
        
        // Filtra los usuarios comprobando si el texto de búsqueda está en el nombre o en el email
        List<User> resultados = todos.stream()
            .filter(u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(query)
                      || u.getEmail().toLowerCase().contains(query))
            .toList();
        return ResponseEntity.ok(resultados);
    }

    /**
     * Comprueba si un usuario específico está online.
     * Endpoint: GET /api/users/online/{id}
     */
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

    /**
     * Marca a un usuario como DESCONECTADO (Offline).
     * Endpoint: POST /api/users/set-offline/{id}
     */
    @PostMapping("/set-offline/{id}")
    public ResponseEntity<?> setOffline(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setOnline(false);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
        return ResponseEntity.ok("OK");
    }

    /**
     * Marca a un usuario como CONECTADO (Online).
     * Endpoint: POST /api/users/set-online/{id}
     */
    @PostMapping("/set-online/{id}")
    public ResponseEntity<?> setOnline(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
        return ResponseEntity.ok("OK");
    }

    /**
     * Devuelve la lista de amigos junto con su estado de conexión (Online/Offline y última vez visto).
     * Endpoint: GET /api/users/amigos/estado/{id}
     */
    @GetMapping("/amigos/estado/{id}")
    public ResponseEntity<List<Map<String, Object>>> amigosConEstado(@PathVariable Long id) {
        User usuario = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Mapea la lista de amigos a una estructura simple (ID, Nombre, Foto, Online) para el frontend
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
}



