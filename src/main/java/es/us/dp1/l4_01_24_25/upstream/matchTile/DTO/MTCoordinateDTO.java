package es.us.dp1.l4_01_24_25.upstream.matchTile.DTO;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import lombok.Getter;

@Getter
public class MTCoordinateDTO {
    
    Integer id;

    Coordinate coordinate;

    public MTCoordinateDTO(Integer id, Coordinate coordinate) {
        this.id = id;
        this.coordinate = coordinate;
    }
}