package upstream.developers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/developers")
public class DevelopersController {
    
    private volatile List<Developer> developers;

    @GetMapping
    public List<Developer> getDevelopers() {
        if (developers == null) {
            loadDevelopers();
        }
        return developers;        
    }

    private synchronized void loadDevelopers() {
        if (developers != null) {
            return;
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();
        File file = new File("pom.xml");
        if (file.exists()) {
            try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8)) {
                Model model = reader.read(fileReader);
                developers = model.getDevelopers() != null ? model.getDevelopers() : Collections.emptyList();
                return;
            } catch (IOException | XmlPullParserException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading pom.xml file", e);
            }
        }

        // Fallback for packaged JAR / Docker runtime where pom.xml is packaged in classpath
        try (InputStream is = getClass().getResourceAsStream("/META-INF/maven/github.ignacio0407/upstream-board-game/pom.xml")) {
            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    Model model = reader.read(isr);
                    developers = model.getDevelopers() != null ? model.getDevelopers() : Collections.emptyList();
                    return;
                }
            }
        } catch (IOException | XmlPullParserException e) {
            // Ignore classpath read error and continue to fallback
        }

        // Default developer fallback if pom is not found anywhere
        Developer dev = new Developer();
        dev.setName("Ignacio Gutierrez Serrera");
        dev.setEmail("ignaciogutierrezserrera@gmail.com");
        dev.setUrl("https://github.com/Ignacio0407");
        developers = List.of(dev);
    }
}