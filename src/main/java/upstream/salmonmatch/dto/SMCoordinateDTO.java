package upstream.salmonmatch.dto;

import upstream.coordinate.Coordinate;
import lombok.Getter;

@Getter
public class SMCoordinateDTO {
    
    Coordinate coordinate;

    public SMCoordinateDTO(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}