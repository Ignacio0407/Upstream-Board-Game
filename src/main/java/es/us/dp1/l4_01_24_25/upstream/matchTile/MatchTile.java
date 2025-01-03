package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.MatchDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.MatchSerializer;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name="match_tile")
public class MatchTile extends BaseEntity {

    Integer capacity;

    Integer orientation;

    List<Integer> jumpingSides;

    @Embedded
    Coordinate coordinate;

    @JsonSerialize(using = TileSerializer.class)
    @JsonDeserialize(using = TileDeserializer.class)
    @ManyToOne
    Tile tile;

    @ManyToOne
    @JoinColumn(name = "match_id")
    @JsonSerialize(using = MatchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    Match match;

}
