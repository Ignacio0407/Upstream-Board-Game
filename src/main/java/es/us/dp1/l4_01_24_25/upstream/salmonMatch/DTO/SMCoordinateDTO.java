package es.us.dp1.l4_01_24_25.upstream.salmonMatch.DTO;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import lombok.Getter;

@Getter
public class SMCoordinateDTO {
    
    Coordinate coordinate;

    public SMCoordinateDTO(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}