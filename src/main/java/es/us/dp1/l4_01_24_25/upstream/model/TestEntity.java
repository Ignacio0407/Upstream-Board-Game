package es.us.dp1.l4_01_24_25.upstream.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TestEntity extends BaseEntity {
    private String name;
}
