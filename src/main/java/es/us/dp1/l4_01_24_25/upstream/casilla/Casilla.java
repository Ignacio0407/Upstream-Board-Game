package es.us.dp1.l4_01_24_25.upstream.casilla;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
public class Casilla extends BaseEntity {
    String imagen;
    TipoCasilla tipo;
}
