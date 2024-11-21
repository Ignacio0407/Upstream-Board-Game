package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="salmonMatch")
@Getter
@Setter
public class salmonMatch extends BaseEntity{
    Player player;
    Integer salmonsNumber;
    Integer spawningNumber;
    
    @NotNull
    @ManyToOne(optional=false)
    @JoinColumn(name="Salmon")
    Salmon salmon;


    @ManyToOne
    @JoinColumn(name = "matches", nullable = false)
    private Match match;

}
