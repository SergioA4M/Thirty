package thirty_api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuraci\u00f3n general del entorno Web (rutas est\u00e1ticas y CORS).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // NOTA: Como ahora usamos Supabase (en el UploadController), 
        // ya no necesitamos mapear recursos locales de "/uploads/**".
        // Sin embargo, por seguridad si tienes algo est\u00e1tico extra, se puede dejar as\u00ed:
        // registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
