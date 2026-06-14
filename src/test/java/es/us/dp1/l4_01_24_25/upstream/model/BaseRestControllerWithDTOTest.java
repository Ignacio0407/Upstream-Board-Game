package es.us.dp1.l4_01_24_25.upstream.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.us.dp1.l4_01_24_25.upstream.exceptions.GlobalExceptionHandler;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestDTO;

@ExtendWith(MockitoExtension.class)
public class BaseRestControllerWithDTOTest {

    @Mock
    private BaseServiceWithDTO<TestEntity, TestDTO, Integer> service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TestControllerWithDTO controller = new TestControllerWithDTO(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("5.1 GET / (findAll) - Happy path")
    void findAll_Happy() throws Exception {
        TestDTO d1 = new TestDTO(1, "D1");
        when(service.findAllAsDTO()).thenReturn(List.of(d1));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("D1"));
    }

    @Test
    @DisplayName("5.2 GET / (findAll) - Edge path (empty)")
    void findAll_Edge() throws Exception {
        when(service.findAllAsDTO()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @DisplayName("5.3 GET /{id} (findById) - Happy path")
    void findById_Happy() throws Exception {
        TestDTO d1 = new TestDTO(1, "D1");
        when(service.findByIdAsDTO(1)).thenReturn(d1);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("5.4 GET /{id} (findById) - Negative path (404)")
    void findById_Negative() throws Exception {
        when(service.findByIdAsDTO(99)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5.5 POST / (save) - Happy path")
    void save_Happy() throws Exception {
        TestDTO saved = new TestDTO(1, "Test");
        when(service.saveAsDTO(any())).thenReturn(saved);

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @DisplayName("5.6 POST / (save) - Edge path (malformed JSON -> 400)")
    void save_Edge_MalformedJson() throws Exception {
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json"))
                .andExpect(status().isBadRequest());

        verify(service, never()).saveAsDTO(any());
    }

    @Test
    @DisplayName("5.7 PUT /{id} (update) - Happy path")
    void update_Happy() throws Exception {
        TestDTO updated = new TestDTO(1, "Updated");
        when(service.updateAsDTO(eq(1), any())).thenReturn(updated);

        mockMvc.perform(put("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @DisplayName("5.8 PUT /{id} (update) - Negative path (404)")
    void update_Negative() throws Exception {
        when(service.updateAsDTO(eq(99), any())).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5.9 DELETE /{id} (delete) - Happy path")
    void delete_Happy() throws Exception {
        mockMvc.perform(delete("/1"))
                .andExpect(status().isNoContent());
        verify(service).deleteById(1);
    }

    @Test
    @DisplayName("5.10 DELETE /{id} (delete) - Negative path (404)")
    void delete_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(service).deleteById(99);

        mockMvc.perform(delete("/99"))
                .andExpect(status().isNotFound());
    }
}