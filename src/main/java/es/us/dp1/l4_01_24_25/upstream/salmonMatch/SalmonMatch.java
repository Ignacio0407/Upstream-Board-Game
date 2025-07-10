package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="salmonMatches")
public class SalmonMatch extends BaseEntity{

    @ManyToOne
    Player player;
    
    @ValidNumber(min=0,max=2)
    Integer salmonsNumber;

    @ValidNumber(min=0,max=5)
    Integer spawningNumber;
    
    @Embedded
    Coordinate coordinate;
    
    @NotNull
    @ManyToOne
    Salmon salmon;

    @ManyToOne
    @JoinColumn(name = "matches", nullable = false)
    private Match match;

}