package thirty_api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion general del entorno Web (rutas esteticas y CORS).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // NOTA: Como ahora usamos Supabase (en el UploadController), 
        // ya no necesitamos mapear recursos locales de "/uploads/**".
        // Sin embargo, por seguridad si tienes algo estetico extra, se puede dejar asi:
        // registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
