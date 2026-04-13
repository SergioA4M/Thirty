package thirty_api.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        // Esto hace que al entrar a la web, lo primero que vean sea el login
        return "forward:/login.html";
    }
}