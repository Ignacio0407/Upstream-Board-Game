package es.us.dp1.l4_01_24_25.upstream.partidaCasilla;

import es.us.dp1.l4_01_24_25.upstream.casilla.Casilla;
import es.us.dp1.l4_01_24_25.upstream.coordenada.Coordenada;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="partidaCasilla")
public class PartidaCasilla extends BaseEntity {

    Integer orientacion;
    Integer capacidad;
    @Embedded
    Coordenada coordenada;
    @NotNull
    @OneToOne(optional=false)
    @JoinColumn(name="Casilla")
    Casilla casilla;




    
}
