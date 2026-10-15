package upstream.player;

import upstream.match.Match;
import upstream.model.BaseEntity;
import upstream.user.User;
import upstream.validation.ValidNumber;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public class Player extends BaseEntity{
    
    String name;
    
    @Enumerated(EnumType.STRING)
    Color color;
    
    Integer playerOrder;
    
    Boolean alive;

    @ValidNumber(min=0, max=28)
    Integer points;
    
    @ValidNumber(min=0, max=5)
    Integer energy;

    @ManyToOne
    User userPlayer;
    
    @ManyToOne
    Match match;

}