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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import es.us.dp1.l4_01_24_25.upstream.exceptions.ResourceNotFoundException;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.NoOverrideTestService;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestRepository;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestService;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {
    
    @Mock
    private TestRepository repository;

    private TestService service;
    private NoOverrideTestService noOverrideService;

    @BeforeEach
    void setUp() {
        service = new TestService(repository);
        noOverrideService = new NoOverrideTestService(repository);
    }

    @Test
    @DisplayName("1.1 extractEntityName - Happy path")
    void extractEntityName_Happy() {
        assertThat(service.extractEntityName(repository)).isEqualTo("TestEntity");
    }

    @Test
    @DisplayName("1.2 extractEntityName - Edge path (reflection failure -> \"Resource\")")
    void extractEntityName_Edge() {
        // Passing a null repository makes repository.getClass() throw a
        // NullPointerException, which is caught internally and results in
        // the fallback entity name "Resource".
        assertThat(service.extractEntityName(null)).isEqualTo("Resource");
    }

    @Test
    @DisplayName("1.3 findOrResourceNotFoundException - Happy path")
    void findOrResourceNotFoundException_Happy() {
        TestEntity entity = new TestEntity();
        TestEntity result = service.findOrResourceNotFoundException(Optional.of(entity), "ID", 1);
        assertThat(result).isSameAs(entity);
    }

    @Test
    @DisplayName("1.4 findOrResourceNotFoundException - Negative path")
    void findOrResourceNotFoundException_Negative() {
        assertThatThrownBy(() -> service.findOrResourceNotFoundException(Optional.empty(), "ID", 99))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("not found with ID: 99");
    }

    @Test
    @DisplayName("1.5 findAll - Happy path")
    void findAll_Happy() {
        List<TestEntity> list = List.of(new TestEntity(), new TestEntity());
        when(repository.findAll()).thenReturn(list);
        assertThat(service.findAll()).hasSize(2).containsExactlyElementsOf(list);
    }

    @Test
    @DisplayName("1.6 findAll - Edge path (empty)")
    void findAll_Edge() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("1.7 findById - Happy path")
    void findById_Happy() {
        TestEntity entity = new TestEntity();
        when(repository.findById(1)).thenReturn(Optional.of(entity));
        assertThat(service.findById(1)).isSameAs(entity);
    }

    @Test
    @DisplayName("1.8 findById - Negative path")
    void findById_Negative() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("1.9 save - Happy path")
    void save_Happy() {
        TestEntity entity = new TestEntity();
        when(repository.save(entity)).thenReturn(entity);
        assertThat(service.save(entity)).isSameAs(entity);
        verify(repository, times(1)).save(entity);
    }

    // 1.10 save - Negative path (validation failure on @Valid) is intentionally
    // NOT covered here: it requires the service to run behind a Spring
    // @Validated proxy (method validation), which a plain Mockito unit test
    // does not exercise. Writing a test here would not actually validate
    // anything and would be a "filler" test. Cover this scenario instead in
    // a @SpringBootTest / slice test if/when needed.

    @Test
    @DisplayName("1.11 saveAll - Happy path")
    void saveAll_Happy() {
        List<TestEntity> list = List.of(new TestEntity());
        when(repository.saveAll(list)).thenReturn(list);
        assertThat(service.saveAll(list)).isSameAs(list);
    }

    @Test
    @DisplayName("1.12 saveAll - Edge path (empty list)")
    void saveAll_Edge() {
        when(repository.saveAll(List.of())).thenReturn(List.of());
        assertThat(service.saveAll(List.of())).isEmpty();
        verify(repository).saveAll(List.of());
    }

    @Test
    @DisplayName("1.13 deleteById - Happy path")
    void deleteById_Happy() {
        service.deleteById(1);
        verify(repository).deleteById(1);
    }

    @Test
    @DisplayName("1.14 deleteById - Negative path (propagates exception)")
    void deleteById_Negative() {
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(99);
        assertThatThrownBy(() -> service.deleteById(99))
            .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("1.15 delete - Happy path")
    void delete_Happy() {
        TestEntity entity = new TestEntity();
        service.delete(entity);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("1.16 delete - Edge path (propagates exception for transient entity)")
    void delete_Edge() {
        TestEntity transientEntity = new TestEntity();
        doThrow(new IllegalArgumentException("Entity not managed")).when(repository).delete(transientEntity);
        assertThatThrownBy(() -> service.delete(transientEntity))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("1.17 deleteAllInBatch - Happy path")
    void deleteAllInBatch_Happy() {
        List<TestEntity> list = List.of(new TestEntity());
        service.deleteAllInBatch(list);
        verify(repository).deleteAllInBatch(list);
    }

    @Test
    @DisplayName("1.18 deleteAllInBatch - Edge path (empty list)")
    void deleteAllInBatch_Edge() {
        service.deleteAllInBatch(List.of());
        verify(repository).deleteAllInBatch(List.of());
    }

    @Test
    @DisplayName("1.19 deleteAll - Happy path")
    void deleteAll_Happy() {
        List<TestEntity> list = List.of(new TestEntity());
        service.deleteAll(list);
        verify(repository).deleteAll(list);
    }

    @Test
    @DisplayName("1.20 deleteAll - Edge path (empty list)")
    void deleteAll_Edge() {
        service.deleteAll(List.of());
        verify(repository).deleteAll(List.of());
    }

    @Test
    @DisplayName("1.21 update - Happy path")
    void update_Happy() {
        TestEntity existing = new TestEntity();
        existing.setId(1);
        existing.setName("Old");
        TestEntity updated = new TestEntity();
        updated.setName("New");

        when(repository.findById(1)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        TestEntity result = service.update(1, updated);
        assertThat(result.getName()).isEqualTo("New");
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("1.22 update - Negative path (not found)")
    void update_Negative() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        TestEntity entityToUpdate = new TestEntity();
        assertThatThrownBy(() -> service.update(99, entityToUpdate))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("1.23 update - Edge path (UnsupportedOperationException)")
    void update_Edge_NoOverride() {
        TestEntity existing = new TestEntity();
        when(repository.findById(1)).thenReturn(Optional.of(existing));
        TestEntity entityToUpdate = new TestEntity();
        assertThatThrownBy(() -> noOverrideService.update(1, entityToUpdate))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("updateEntityFields no implementado");
    }

    @Test
    @DisplayName("1.24 updateSome - Happy path")
    void updateSome_Happy() {
        TestEntity e1 = new TestEntity(); e1.setId(1);
        TestEntity e2 = new TestEntity(); e2.setId(2);
        TestEntity u1 = new TestEntity(); u1.setName("N1");
        TestEntity u2 = new TestEntity(); u2.setName("N2");

        when(repository.findById(1)).thenReturn(Optional.of(e1));
        when(repository.findById(2)).thenReturn(Optional.of(e2));
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<TestEntity> result = service.updateSome(List.of(1, 2), List.of(u1, u2));
        assertThat(result).hasSize(2);
        assertThat(e1.getName()).isEqualTo("N1");
        assertThat(e2.getName()).isEqualTo("N2");
    }

    @Test
    @DisplayName("1.25 updateSome - Negative path (size mismatch)")
    void updateSome_Negative_Mismatch() {
        List<Integer> ids = List.of(1);
        List<TestEntity> entities = List.of(new TestEntity(), new TestEntity());
        assertThatThrownBy(() -> service.updateSome(ids, entities))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("1.26 updateSome - Negative path (one id not found)")
    void updateSome_Negative_NotFound() {
        TestEntity e1 = new TestEntity(); e1.setId(1);

        when(repository.findById(1)).thenReturn(Optional.of(e1));
        when(repository.findById(99)).thenReturn(Optional.empty());

        List<Integer> ids = List.of(1, 99);
        List<TestEntity> entities = List.of(new TestEntity(), new TestEntity());

        assertThatThrownBy(() -> service.updateSome(ids, entities))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("1.27 updateSome - Edge path (empty lists)")
    void updateSome_Edge_Empty() {
        when(repository.saveAll(List.of())).thenReturn(List.of());

        List<TestEntity> result = service.updateSome(List.of(), List.of());

        assertThat(result).isEmpty();
        verify(repository).saveAll(List.of());
    }
}