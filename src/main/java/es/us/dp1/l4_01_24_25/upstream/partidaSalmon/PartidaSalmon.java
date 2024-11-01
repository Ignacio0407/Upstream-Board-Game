package es.us.dp1.l4_01_24_25.upstream.partidaSalmon;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.partida.Partida;
import es.us.dp1.l4_01_24_25.upstream.player.Jugador;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="partidaSalmon")
public class PartidaSalmon extends BaseEntity{
    Jugador jugador;
    Integer nSalmones;
    Integer nDesove;
    
    @NotNull
    @ManyToOne(optional=false)
    @JoinColumn(name="Salmon")
    Salmon salmon;


    @ManyToOne
    @JoinColumn(name = "matches", nullable = false)
    private Partida partida;

}
