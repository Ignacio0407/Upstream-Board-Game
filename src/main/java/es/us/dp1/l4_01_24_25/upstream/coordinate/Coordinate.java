package es.us.dp1.l4_01_24_25.upstream.coordinate;

import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import jakarta.persistence.Embeddable;

@Embeddable
public record Coordinate(

    @ValidNumber(min = 0, max = 2)
    Integer x,

    @ValidNumber(min = 0, max = 25)
    Integer y

) {}