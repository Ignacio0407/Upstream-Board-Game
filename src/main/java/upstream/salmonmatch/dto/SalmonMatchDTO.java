package upstream.salmonmatch.dto;

import upstream.coordinate.Coordinate;
import upstream.salmon.Salmon;
import upstream.validation.ValidNumber;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalmonMatchDTO {
    
    Integer id;
    
    Integer playerId;
    
    @ValidNumber(min=0,max=5)
    Integer salmonsNumber;

    @ValidNumber(min=0,max=5)
    Integer spawningNumber;
    
    @Valid
    Coordinate coordinate;
    
    Salmon salmon;

    Integer matchId;

    public SalmonMatchDTO() {}
    public SalmonMatchDTO(Integer id, Integer playerId, Integer salmonsNumber, Integer spawningNumber, Coordinate coordinate, Salmon salmon, Integer matchId) {
        this.id = id;
        this.playerId = playerId;
        this.salmonsNumber = salmonsNumber;
        this.spawningNumber = spawningNumber;
        this.coordinate = coordinate;
        this.salmon = salmon;
        this.matchId = matchId;
    }

}
