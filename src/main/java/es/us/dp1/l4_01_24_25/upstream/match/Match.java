package es.us.dp1.l4_01_24_25.upstream.match;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.model.NamedEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.UserDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.UserSerializer;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "matches")
@Table(name = "matches")
@Inheritance(strategy = InheritanceType.JOINED)
public class Match extends NamedEntity {

	// Esto para unirse
	String password;
	@Enumerated(EnumType.STRING)
	State state;
	// Esto para jugar en sí
	@Min(0)
	@Max(5)
	Integer playersNum;
    Integer round;
    @Enumerated(EnumType.STRING)
	Phase phase;
	
	@OneToOne
	@JoinColumn(name="initial_player")
	@JsonSerialize(using = PlayerSerializer.class)
	@JsonDeserialize(using = PlayerDeserializer.class)
	Player initialPlayer;
	
	@OneToOne
	@JoinColumn(name="actual_player")
	@JsonSerialize(using = PlayerSerializer.class)
	@JsonDeserialize(using = PlayerDeserializer.class)
	Player actualPlayer;

	@OneToOne
	@JoinColumn(name="match_creator")
	@JsonSerialize(using = UserSerializer.class)
	@JsonDeserialize(using = UserDeserializer.class)
	User matchCreator;

	public Integer getPlayersNum() {
        return playersNum;
    }

    // Método setter para el número de jugadores
    public void setNumJugadores(Integer numjugadores) {
        this.playersNum = numjugadores;
    }

}