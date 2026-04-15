package thirty_api.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Servicio para conectarse con Supabase Storage.
 * Sube los archivos a la nube para que no se borren cuando Render se reinicie.
 */
@Service
public class SupabaseService {

    // Cambia esto por tu Project URL de Supabase
    private final String SUPABASE_URL = "https://ylxpcytucebvzijqyrsx.supabase.co";
    
    // OJO: Tu key actual (sb_publishable_...) parece de otro servicio.
    // Las keys de Supabase SIEMPRE empiezan por "eyJ".
    // Si te da error de autorizaci\u00f3n al subir fotos, ve a Supabase -> Project Settings -> API y copia tu "anon public key".
    private final String API_KEY = "sb_publishable_JzfCE0OJFsPuvfVxyiCGrQ_KP4Z7gcg";

    /**
     * Sube un archivo (byte[]) al bucket "uploads" de Supabase.
     */
    public void uploadFile(byte[] fileBytes, String filename, String contentType) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        // La ruta a la que hay que mandar el POST para subir archivos en Supabase
        String url = SUPABASE_URL + "/storage/v1/object/uploads/" + filename;

        HttpHeaders headers = new HttpHeaders();
        // Supabase necesita tu API Key en estos dos Headers para dejarte subir archivos
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("apikey", API_KEY);
        
        // Le decimos de qu\u00e9 tipo es el archivo (ej: image/png)
        headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"));

        // Preparamos la petici\u00f3n con el archivo y las credenciales
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

        try {
            // Hacemos el POST
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Error HTTP de Supabase: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Fallo al subir a Supabase: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error subiendo a Supabase: " + e.getResponseBodyAsString());
        }
    }
}