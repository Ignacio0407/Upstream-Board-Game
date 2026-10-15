package upstream.match;

import upstream.model.NamedEntity;
import upstream.player.Player;
import upstream.user.User;
import upstream.validation.ValidNumber;
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

	String password;
	
	@Enumerated(EnumType.STRING)
	State state;

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
	User matchCreator;

	public Match() {
		// Empty constructor for tests
	}

}