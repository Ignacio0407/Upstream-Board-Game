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

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestDTO;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestRepository;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestServiceWithDTO;

@ExtendWith(MockitoExtension.class)
public class BaseServiceWithDTOTest {

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