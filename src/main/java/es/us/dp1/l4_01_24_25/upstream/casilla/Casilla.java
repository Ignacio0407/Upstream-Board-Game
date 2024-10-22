package es.us.dp1.l4_01_24_25.upstream.casilla;

import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
public class Casilla {
    String imagen;
    TipoCasilla tipo;
}
