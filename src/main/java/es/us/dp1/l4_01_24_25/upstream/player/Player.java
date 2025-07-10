package es.us.dp1.l4_01_24_25.upstream.player;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
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

    @ValidNumber(min=0,max=60)
    Integer points;
    
    @ValidNumber(min=0,max=5)
    Integer energy;

    @ManyToOne
    @JsonSerialize(using = UserSerializer.class)
    @JsonDeserialize(using = UserDeserializer.class)
    User userPlayer;
    
    @ManyToOne
    Match match;

}