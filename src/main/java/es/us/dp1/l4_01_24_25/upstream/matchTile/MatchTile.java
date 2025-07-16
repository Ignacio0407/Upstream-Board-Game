package es.us.dp1.l4_01_24_25.upstream.matchTile;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileType;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="match_tile")
public class MatchTile extends BaseEntity {

    @ValidNumber(min=0, max=5)
    Integer capacity;

    @ValidNumber(min=0)
    Integer orientation;

    @ValidNumber(min=0, max=5)
    Integer salmonsNumber;

    @Embedded
    Coordinate coordinate;

    @ManyToOne
    Tile tile;

    @ManyToOne
    Match match;

    public Boolean isFull () {
        if (this.match.getPlayersNumber() >= 2 && this.getTile().getType().equals(TileType.ROCK)) return this.getCapacity().equals(this.getSalmonsNumber() -1);
        else return this.getCapacity().equals(this.getSalmonsNumber()); 
    }

}