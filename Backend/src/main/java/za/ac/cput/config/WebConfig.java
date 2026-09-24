/*
 WebConfig.java

 Exposes the uploads directory (see UploadController) at /uploads/** so a
 saved file is reachable over plain HTTP by the URL the upload endpoint hands
 back. Without this, files land on disk but 404 when the browser tries to
 load them.

 Author: Aidan Barends 230255639
 Date: 18 September 2026
*/

package za.ac.cput.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadsDir;

    public WebConfig(@Value("${app.uploads.dir:uploads}") String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(this.uploadsDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

}
