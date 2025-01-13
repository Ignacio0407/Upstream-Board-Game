package es.us.dp1.l4_01_24_25.upstream.salmon;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
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

    @Test
    void testFindAll_Positive() throws Exception {
        List<Salmon> salmons = Arrays.asList(new Salmon(), new Salmon());
        when(salmonService.findAll()).thenReturn(salmons);

        mockMvc.perform(get("/api/v1/salmons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[{},{}]"));
    }

    @Test
    void testFindAll_Empty() throws Exception {
        when(salmonService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/salmons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }

    @Test
    void testFindById_Positive() throws Exception {
        Salmon salmon = new Salmon();
        when(salmonService.findById(1)).thenReturn(Optional.of(salmon));

        mockMvc.perform(get("/api/v1/salmons/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{}"));
    }

    @Test
    void testFindById_NotFound() throws Exception {
        when(salmonService.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/salmons/1"))
            .andExpect(status().isNotFound());
    }
}

