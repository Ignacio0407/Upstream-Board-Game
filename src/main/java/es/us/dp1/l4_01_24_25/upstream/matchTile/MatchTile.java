package es.us.dp1.l4_01_24_25.upstream.matchTile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.casilla.Casilla;
import es.us.dp1.l4_01_24_25.upstream.coordenada.Coordenada;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.partida.Partida;
import es.us.dp1.l4_01_24_25.upstream.player.MatchDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.MatchSerializer;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class MatchTile extends BaseEntity {

    Integer capacity;

    Integer orientation;

    @Embedded
    Coordenada coordinate;

    @JsonSerialize(using = TileSerializer.class)
    @JsonDeserialize(using = TileDeserializer.class)
    @ManyToOne
    Casilla tile;

    @ManyToOne
    @JsonSerialize(using = MatchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    Partida match;

}
