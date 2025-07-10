package es.us.dp1.l4_01_24_25.upstream.matchTile;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchTileDTO {
    
    Integer id;

    @ValidNumber(min=0, max=5)
    Integer capacity;

    @ValidNumber(min=0)
    Integer orientation;

    @ValidNumber(min=0, max=5)
    Integer salmonsNumber;

    @Embedded
    Coordinate coordinate;

    Tile tile;

    Integer matchId;

    public MatchTileDTO(Integer id, Integer capacity, Integer orientation, Integer salmonsNumber, Coordinate coordinate, Tile tile, Integer matchId) {
        this.id = id;
        this.capacity = capacity;
        this.orientation = orientation;
        this.salmonsNumber = salmonsNumber;
        this.coordinate = coordinate;
        this.tile = tile;
        this.matchId = matchId;
    }

    public MatchTileDTO() {}

}