package es.us.dp1.l4_01_24_25.upstream.salmonMatch;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.coordinate.Coordinate;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.PlayerDeserializer;
import es.us.dp1.l4_01_24_25.upstream.match.PlayerSerializer;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.MatchDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.MatchSerializer;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.salmon.Salmon;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="salmonMatches")
@Getter
@Setter
public class SalmonMatch extends BaseEntity{

    @ManyToOne
    @JsonSerialize(using = PlayerSerializer.class)
	@JsonDeserialize(using = PlayerDeserializer.class)
    Player player;
    
    Integer salmonsNumber;
    Integer spawningNumber;
    @Embedded
    Coordinate coordinate;
    
    @NotNull
    @ManyToOne
    @JsonSerialize(using = SalmonSerializer.class)
	@JsonDeserialize(using = SalmonDeserializer.class)
    Salmon salmon;

    @ManyToOne
    @JoinColumn(name = "matches", nullable = false)
    @JsonSerialize(using = MatchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    private Match match;

}
