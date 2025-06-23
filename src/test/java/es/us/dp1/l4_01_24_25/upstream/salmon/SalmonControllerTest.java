package es.us.dp1.l4_01_24_25.upstream.salmon;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SalmonController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class))
@AutoConfigureMockMvc(addFilters = false)
public class SalmonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalmonService salmonService;

    @ParameterizedTest
    @CsvSource({
        "2, '[{},{}]', 1",
        "0, '[]', 0"
    })
    void testFindAll(int expectedSize, String expectedJsonResponse, int salmonCount) throws Exception {
        List<Salmon> salmons = salmonCount > 0 ? Arrays.asList(new Salmon(), new Salmon()) : List.of();
        when(salmonService.findAll()).thenReturn(salmons);

        mockMvc.perform(get("/api/v1/salmons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJsonResponse));
    }

    @ParameterizedTest
    @CsvSource({
        "1, '{}', true",  // Recurso encontrado, se espera un JSON vacío
        "999, null, false"  // Recurso no encontrado, no se espera cuerpo
    })
    void testFindById(int id, String expectedJsonResponse, boolean exists) throws Exception {

        if (exists) {
            mockMvc.perform(get("/api/v1/salmons/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJsonResponse));
        } else {
            mockMvc.perform(get("/api/v1/salmons/" + id))
                .andExpect(status().isNotFound());  // Verifica el 404 sin cuerpo
        }
    }

}