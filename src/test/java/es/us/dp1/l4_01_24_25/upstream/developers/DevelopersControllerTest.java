package es.us.dp1.l4_01_24_25.upstream.developers;

import org.apache.maven.model.Developer;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class DevelopersControllerTest {

    @Test
    void testGetDevelopers_LoadsPomAndCaches() {
        DevelopersController controller = new DevelopersController();

        List<Developer> devList1 = controller.getDevelopers();
        assertNotNull(devList1);
        assertFalse(devList1.isEmpty());

        // Second call should return cached list without re-reading pom.xml
        List<Developer> devList2 = controller.getDevelopers();
        assertSame(devList1, devList2);
    }

    @Test
    void testLoadDevelopers_ExceptionHandling() {
        try (MockedConstruction<MavenXpp3Reader> mocked = mockConstruction(MavenXpp3Reader.class, (mock, context) -> {
            when(mock.read(any(FileReader.class))).thenThrow(new IOException("File read error"));
        })) {
            DevelopersController controller = new DevelopersController();
            assertThrows(ResponseStatusException.class, controller::getDevelopers);
        }
    }
}
