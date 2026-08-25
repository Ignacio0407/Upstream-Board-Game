/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

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

import es.us.dp1.l4_01_24_25.upstream.exceptions.ExceptionHandlerController;
import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestControllerWithDTO;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestDTO;

@ExtendWith(MockitoExtension.class)
class BaseRestControllerWithDTOTest {

    @Mock
    private BaseServiceWithDTO<TestEntity, TestDTO, Integer> service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TestControllerWithDTO controller = new TestControllerWithDTO(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ExceptionHandlerController()).build();
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