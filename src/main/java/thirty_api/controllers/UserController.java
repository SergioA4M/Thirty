package thirty_api.controllers;

import thirty_api.models.User;
import thirty_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Este método nos devolverá todos los usuarios para probar
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Este método servirá para registrar un usuario nuevo
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}
