package thirty_api.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controlador de Vistas (ViewController).
 *
 * ¿Qué es esto?
 * A diferencia de los @RestController (que devuelven datos puros en JSON),
 * un @Controller normal en Spring Boot se utiliza habitualmente para 
 * devolver o redirigir a páginas web enteras (archivos HTML).
 * 
 * En nuestro caso, como usamos HTML puro en la carpeta "static", 
 * este controlador solo nos sirve para una cosa muy específica: 
 * redirigir a la gente a la pantalla de inicio de sesión.
 */
@Controller // Indica que esta clase devuelve Vistas (Vistas = páginas web HTML).
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS}, allowedHeaders = "*") // Permite acceso desde cualquier origen
public class ViewController {

    /**
     * Endpoint raíz del proyecto ("/").
     * Cuando alguien entra a "http://localhost:8080" (o a la URL de Render) sin poner nada más...
     * ¿Qué debe pasar? Queremos que vaya directo al Login.
     */
    @GetMapping("/")
    public String index() {
        // "forward:/login.html" le dice a Spring Boot:
        // "Busca el archivo 'login.html' en la carpeta 'static' y muéstraselo al usuario".
        // A diferencia del "redirect:", la URL en el navegador seguirá diciendo "/", 
        // pero el contenido será el de la página de login.
        return "forward:/login.html";
    }
}