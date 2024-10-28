package es.us.dp1.l4_01_24_25.upstream.casilla;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "casilla")
public class Casilla extends BaseEntity {
    String imagen;
    
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo")
    TipoCasilla tipo;
}
