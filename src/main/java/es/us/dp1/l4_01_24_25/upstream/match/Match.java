package es.us.dp1.l4_01_24_25.upstream.match;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.model.NamedEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.UserDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.UserSerializer;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "matches")
@Inheritance(strategy = InheritanceType.JOINED)
public class Match extends NamedEntity {

	// Esto para unirse
	String password;
	
	@Enumerated(EnumType.STRING)
	State state;
	
	// Esto para jugar en sí
	@ValidNumber(min=0,max=5)
	Integer playersNumber;
    
	@ValidNumber(min=0)
	Integer round;
    
	@Enumerated(EnumType.STRING)
	Phase phase;

	Boolean finalScoreCalculated;

	@OneToOne
	@JoinColumn(name="initial_player")
	Player initialPlayer;
	
	@OneToOne
	@JoinColumn(name="actual_player")
	Player actualPlayer;

	@ManyToOne
	@JoinColumn(name="match_creator")
	@JsonSerialize(using = UserSerializer.class)
	@JsonDeserialize(using = UserDeserializer.class)
	User matchCreator;

	public Match() {}

}