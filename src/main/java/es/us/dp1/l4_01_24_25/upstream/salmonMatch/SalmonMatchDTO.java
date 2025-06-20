package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
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
    
    Coordinate coordinate;
    
    Salmon salmon;

    Integer matchId;

}