package es.us.dp1.l4_01_24_25.upstream.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import es.us.dp1.l4_01_24_25.upstream.model.dummy.TestDTO;
import es.us.dp1.l4_01_24_25.upstream.model.dummy.ConcreteTestRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BaseRepositoryTest {

    @Autowired
    private ConcreteTestRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findAllAsDTO - Happy Path: Returns all entities as DTOs")
    void findAllAsDTO_Happy() {
        TestEntity e1 = new TestEntity();
        e1.setName("Entity 1");
        entityManager.persist(e1);

        TestEntity e2 = new TestEntity();
        e2.setName("Entity 2");
        entityManager.persist(e2);

        List<TestDTO> dtos = repository.findAllAsDTO();

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(TestDTO::getName).containsExactlyInAnyOrder("Entity 1", "Entity 2");
    }

    @Test
    @DisplayName("findAllAsDTO - Edge Case: Returns empty list when no entities exist")
    void findAllAsDTO_Empty() {
        List<TestDTO> dtos = repository.findAllAsDTO();
        assertThat(dtos).isEmpty();
    }

    @Test
    @DisplayName("findByIdAsDTO - Happy Path: Returns DTO for existing ID")
    void findByIdAsDTO_Happy() {
        TestEntity e = new TestEntity();
        e.setName("Target");
        entityManager.persist(e);

        Optional<TestDTO> dto = repository.findByIdAsDTO(e.getId());

        assertThat(dto).isPresent();
        assertThat(dto.get().getName()).isEqualTo("Target");
        assertThat(dto.get().getId()).isEqualTo(e.getId());
    }

    @Test
    @DisplayName("findByIdAsDTO - Negative Path: Returns empty for non-existent ID")
    void findByIdAsDTO_NotFound() {
        Optional<TestDTO> dto = repository.findByIdAsDTO(999);
        assertThat(dto).isEmpty();
    }

    @Test
    @DisplayName("findByIdAsDTO - Edge Case: Returns empty for null ID")
    void findByIdAsDTO_NullId() {
        Optional<TestDTO> dto = repository.findByIdAsDTO(null);
        assertThat(dto).isEmpty();
    }
}
