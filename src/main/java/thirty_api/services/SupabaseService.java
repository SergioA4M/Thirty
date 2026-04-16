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
    
    // La key 'anon' pública de tu proyecto Supabase (Empieza por eyJ...)
    private final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlseHBjeXR1Y2VidnppanF5cnN4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU4MDUzNTAsImV4cCI6MjA5MTM4MTM1MH0.r9XQdT60WsoVX9NT5AOvEo6PP7Y2HeKr0TN8qKTMBz8";

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
        
        // Le decimos de qué tipo es el archivo (ej: image/png)
        headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"));

        // Preparamos la petición con el archivo y las credenciales
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

    /**
     * Borra un archivo físicamente de Supabase Storage.
     */
    public void deleteFile(String filename) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = SUPABASE_URL + "/storage/v1/object/uploads/" + filename;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);
            headers.set("apikey", API_KEY);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            System.out.println("Archivo " + filename + " borrado de Supabase exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al intentar borrar el archivo de Supabase: " + e.getMessage());
            // No lanzamos la excepción para que no rompa la aplicación si el archivo ya no existía
        }
    }
}