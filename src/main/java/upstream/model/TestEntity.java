package upstream.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TestEntity extends BaseEntity {
    private String name;
}
