package es.us.dp1.l4_01_24_25.upstream.partidaCasilla;

import es.us.dp1.l4_01_24_25.upstream.casilla.Casilla;
import es.us.dp1.l4_01_24_25.upstream.coordenada.Coordenada;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.partidas.Partida;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @ManyToOne(optional=false)
    @JoinColumn(name="Casilla")
    Casilla casilla;
    @ManyToOne
    @JoinColumn(name = "matches", nullable = false)
    private Partida partida;




    
}
