package upstream.salmonmatch;

import upstream.coordinate.Coordinate;
import upstream.match.Match;
import upstream.model.BaseEntity;
import upstream.player.Player;
import upstream.salmon.Salmon;
import upstream.validation.ValidNumber;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
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
    @Valid
    Coordinate coordinate;
    
    @NotNull
    @ManyToOne
    Salmon salmon;

    @ManyToOne
    @JoinColumn(name = "matches", nullable = false)
    private Match match;

}