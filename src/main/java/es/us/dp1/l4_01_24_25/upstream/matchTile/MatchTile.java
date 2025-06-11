package es.us.dp1.l4_01_24_25.upstream.matchTile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.MatchDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.matchSerializer;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
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

    @ValidNumber(min=0)
    Integer capacity;

    @ValidNumber(min=0, max=6)
    Integer orientation;

    @ValidNumber(min=0, max=5)
    Integer salmonsNumber;

    @Embedded
    Coordinate coordinate;

    @JsonSerialize(using = TileSerializer.class)
    @JsonDeserialize(using = TileDeserializer.class)
    @ManyToOne
    Tile tile;

    @ManyToOne
    @JoinColumn(name = "match_id")
    @JsonSerialize(using = matchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    Match match;

}