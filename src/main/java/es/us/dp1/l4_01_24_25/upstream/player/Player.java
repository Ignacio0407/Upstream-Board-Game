package es.us.dp1.l4_01_24_25.upstream.player;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "player") // -> Esto solo tiene la clase padre
public class Player extends BaseEntity implements Serializable{
    
    String name;
    @Enumerated(EnumType.STRING)
    Color color;
    @Column(name="player_order")
    Integer playerOrder;
    Boolean alive;
    Integer points;

    @JsonSerialize(using = UserSerializer.class)
    @JsonDeserialize(using = UserDeserializer.class)
    @ManyToOne
    @JoinColumn(name="user_player")
    User userPlayer;

    
    @JsonSerialize(using = MatchSerializer.class)
    @JsonDeserialize(using = MatchDeserializer.class)
    @ManyToOne
    @JoinColumn(name="partida")
    Match match;

    @Override
    public String toString() {
    return "Player{" +
            "id=" + getId() + // Esto asume que la clase BaseEntity tiene un campo id con su getter.
            ", name='" + name + '\'' +
            ", color=" + color +
            ", playerOrder=" + playerOrder +
            ", alive=" + alive +
            ", points=" + points +
            ", userPlayer=" + (userPlayer != null ? userPlayer.getId() : "null") + // Evita acceder a objetos nulos.
            ", match=" + (match != null ? match.getId() : "null") +
            '}';
}

}