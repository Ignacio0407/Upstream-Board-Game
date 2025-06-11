package es.us.dp1.l4_01_24_25.upstream.player;

import java.io.Serializable;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "player") // -> Esto solo tiene la clase padre
public class Player extends BaseEntity implements Serializable{
    
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
    @JsonSerialize(using = matchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    Match match;

    @Override
    public String toString() {
    return "Player{" +
            "id=" + getId() +
            ", name='" + name + '\'' +
            ", color=" + color +
            ", playerOrder=" + playerOrder +
            ", alive=" + alive +
            ", points=" + points +
            ", userPlayer=" + (userPlayer != null ? userPlayer.getId() : "null") +
            ", match=" + (match != null ? match.getId() : "null") +
            '}';
    }

}