package upstream.matchtile.dto;

import upstream.coordinate.Coordinate;
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