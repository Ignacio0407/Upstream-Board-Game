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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import upstream.exceptions.ResourceNotFoundException;
import upstream.model.dummy.TestDTO;
import upstream.model.dummy.TestRepository;
import upstream.model.dummy.TestServiceWithDTO;

@ExtendWith(MockitoExtension.class)
class BaseServiceWithDTOTest {

    @Mock
    private TestRepository repository;
    @Mock
    private EntityMapper<TestEntity, TestDTO> mapper;

    private TestServiceWithDTO service;

    @BeforeEach
    void setUp() {
        service = new TestServiceWithDTO(repository, mapper);
    }

    @Test
    @DisplayName("2.1 findAllAsDTO - Happy path")
    void findAllAsDTO_Happy() {
        List<TestDTO> list = List.of(new TestDTO(), new TestDTO());
        when(repository.findAllAsDTO()).thenReturn(list);
        assertThat(service.findAllAsDTO()).isSameAs(list);
    }

    @Test
    @DisplayName("2.2 findAllAsDTO - Edge path (empty)")
    void findAllAsDTO_Edge() {
        when(repository.findAllAsDTO()).thenReturn(List.of());
        assertThat(service.findAllAsDTO()).isEmpty();
    }

    @Test
    @DisplayName("2.3 findDTOOrResourceNotFoundException - Happy path")
    void findDTOOrResourceNotFoundException_Happy() {
        TestDTO dto = new TestDTO();
        assertThat(service.findDTOOrResourceNotFoundException(Optional.of(dto), 1)).isSameAs(dto);
    }

    @Test
    @DisplayName("2.4 findDTOOrResourceNotFoundException - Negative path")
    void findDTOOrResourceNotFoundException_Negative() {
        assertThatThrownBy(() -> service.findDTOOrResourceNotFoundException(Optional.empty(), 99))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("not found with ID: 99");
    }

    @Test
    @DisplayName("2.5 findByIdAsDTO - Happy path")
    void findByIdAsDTO_Happy() {
        TestDTO dto = new TestDTO();
        when(repository.findByIdAsDTO(1)).thenReturn(Optional.of(dto));
        assertThat(service.findByIdAsDTO(1)).isSameAs(dto);
    }

    @Test
    @DisplayName("2.6 findByIdAsDTO - Negative path")
    void findByIdAsDTO_Negative() {
        when(repository.findByIdAsDTO(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByIdAsDTO(99))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("2.7 saveAsDTO - Happy path")
    void saveAsDTO_Happy() {
        TestDTO dto = new TestDTO();
        TestEntity entity = new TestEntity();
        TestEntity savedEntity = new TestEntity();
        TestDTO resultDto = new TestDTO();

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDTO(savedEntity)).thenReturn(resultDto);

        assertThat(service.saveAsDTO(dto)).isSameAs(resultDto);
    }

    // 2.8 saveAsDTO - Negative path (validation failure on @Valid) is intentionally
    // NOT covered here, for the same reason as 1.10 in BaseServiceMatrixTest:
    // it requires a Spring @Validated proxy (method validation), which a plain
    // Mockito unit test does not exercise.

    @Test
    @DisplayName("2.9 saveAsDTO - Edge path (verifies mapper/repository call order)")
    void saveAsDTO_Edge_CallOrder() {
        TestDTO dto = new TestDTO();
        TestEntity entity = new TestEntity();
        TestEntity savedEntity = new TestEntity();
        TestDTO resultDto = new TestDTO();

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDTO(savedEntity)).thenReturn(resultDto);

        service.saveAsDTO(dto);

        InOrder inOrder = inOrder(mapper, repository);
        inOrder.verify(mapper).toEntity(dto);
        inOrder.verify(repository).save(entity);
        inOrder.verify(mapper).toDTO(savedEntity);
    }

    @Test
    @DisplayName("2.10 updateAsDTO - Happy path")
    void updateAsDTO_Happy() {
        TestEntity existing = new TestEntity();
        existing.setId(1);
        existing.setName("Old");

        TestDTO dto = new TestDTO();
        TestEntity mapped = new TestEntity();
        mapped.setName("New");
        TestDTO resultDto = new TestDTO();

        when(mapper.toEntity(dto)).thenReturn(mapped);
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDTO(existing)).thenReturn(resultDto);

        TestDTO result = service.updateAsDTO(1, dto);

        assertThat(result).isSameAs(resultDto);
        assertThat(existing.getName()).isEqualTo("New");
    }

    @Test
    @DisplayName("2.11 updateAsDTO - Negative path (not found)")
    void updateAsDTO_Negative() {
        TestDTO dto = new TestDTO();
        TestEntity mapped = new TestEntity();

        when(mapper.toEntity(dto)).thenReturn(mapped);
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAsDTO(99, dto))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(mapper, never()).toDTO(any());
    }
}