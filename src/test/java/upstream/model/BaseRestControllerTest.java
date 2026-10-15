/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
 * -----------------------------------------------------------------------------
 */

package upstream.model;

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

import upstream.exceptions.ExceptionHandlerController;
import upstream.exceptions.ResourceNotFoundException;
import upstream.model.dummy.TestController;

@ExtendWith(MockitoExtension.class)
class BaseRestControllerTest {

    @Mock
    private BaseService<TestEntity, Integer> service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TestController controller = new TestController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ExceptionHandlerController()).build();
    }

    @Test
    @DisplayName("4.1 GET / (findAll) - Happy path")
    void findAll_Happy() throws Exception {
        TestEntity e1 = new TestEntity(); e1.setId(1);
        when(service.findAll()).thenReturn(List.of(e1));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("4.2 GET / (findAll) - Edge path (empty)")
    void findAll_Edge() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @DisplayName("4.3 GET /{id} (findById) - Happy path")
    void findById_Happy() throws Exception {
        TestEntity e1 = new TestEntity(); e1.setId(1);
        when(service.findById(1)).thenReturn(e1);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("4.4 GET /{id} (findById) - Negative path (404)")
    void findById_Negative() throws Exception {
        when(service.findById(99)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("4.5 GET /{id} (findById) - Edge path (non-numeric id -> 400)")
    void findById_Edge_NonNumericId() throws Exception {
        // No @ControllerAdvice mapping needed: Spring's default
        // DefaultHandlerExceptionResolver turns a path-variable type
        // conversion failure into 400 before the controller is reached.
        mockMvc.perform(get("/abc"))
                .andExpect(status().isBadRequest());

        verify(service, never()).findById(any());
    }

    @Test
    @DisplayName("4.6 POST / (save) - Happy path")
    void save_Happy() throws Exception {
        TestEntity saved = new TestEntity(); saved.setId(1); saved.setName("Test");
        when(service.save(any())).thenReturn(saved);

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @DisplayName("4.7 POST / (save) - Edge path (malformed JSON -> 400)")
    void save_Edge_MalformedJson() throws Exception {
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json"))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any());
    }

    @Test
    @DisplayName("4.8 PUT /{id} (update) - Happy path")
    void update_Happy() throws Exception {
        TestEntity updated = new TestEntity(); updated.setId(1); updated.setName("Updated");
        when(service.update(eq(1), any())).thenReturn(updated);

        mockMvc.perform(put("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @DisplayName("4.9 PUT /{id} (update) - Negative path (404)")
    void update_Negative() throws Exception {
        when(service.update(eq(99), any())).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("4.10 DELETE /{id} (delete) - Happy path")
    void delete_Happy() throws Exception {
        mockMvc.perform(delete("/1"))
                .andExpect(status().isNoContent());
        verify(service).deleteById(1);
    }

    @Test
    @DisplayName("4.11 DELETE /{id} (delete) - Negative path (404)")
    void delete_Negative() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(service).deleteById(99);

        mockMvc.perform(delete("/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("4.12 PUT /some (updateSome) - Happy path")
    void updateSome_Happy() throws Exception {
        TestEntity e1 = new TestEntity(); e1.setId(1); e1.setName("N1");
        TestEntity e2 = new TestEntity(); e2.setId(2); e2.setName("N2");
        when(service.updateSome(eq(List.of(1, 2)), any())).thenReturn(List.of(e1, e2));

        mockMvc.perform(put("/some")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\": [1, 2], \"entities\": [{\"name\": \"N1\"}, {\"name\": \"N2\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("N1"))
                .andExpect(jsonPath("$[1].name").value("N2"));
    }

    @Test
    @DisplayName("4.13 PUT /some (updateSome) - Negative path (size mismatch -> 400)")
    void updateSome_Negative_Mismatch() throws Exception {
        when(service.updateSome(any(), any()))
                .thenThrow(new IllegalArgumentException("Ids and entities size mismatch"));

        mockMvc.perform(put("/some")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\": [1], \"entities\": [{\"name\": \"N1\"}, {\"name\": \"N2\"}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("4.14 PUT /some (updateSome) - Edge path (empty lists)")
    void updateSome_Edge_Empty() throws Exception {
        when(service.updateSome(List.of(), List.of())).thenReturn(List.of());

        mockMvc.perform(put("/some")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\": [], \"entities\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}