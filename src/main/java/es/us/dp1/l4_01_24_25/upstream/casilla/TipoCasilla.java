package es.us.dp1.l4_01_24_25.upstream.casilla;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_casilla")
public class TipoCasilla extends BaseEntity {
    @Column(length = 20)
	String tipo;
}
