package es.us.dp1.l4_01_24_25.upstream.match.DTO;

import es.us.dp1.l4_01_24_25.upstream.match.Phase;
import es.us.dp1.l4_01_24_25.upstream.match.State;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchDTO {

	Integer id;

	String name;
	
	String password;
	
	State state;
	
	@ValidNumber(min=0,max=5)
	Integer playersNumber;
    
	@ValidNumber(min=0)
	Integer round;

	Phase phase;

	Boolean finalScoreCalculated;

	Integer initialPlayerId;
	
	Integer actualPlayerId;

	Integer matchCreatorId;

	public MatchDTO(Integer id, String name, String password, State state, Integer playersNumber,Integer round, Phase phase, Boolean finalScoreCalculated, Integer initialPlayerId, Integer actualPlayerId, Integer matchCreatorId) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.state = state;
		this.playersNumber = playersNumber;
		this.round = round;
		this.phase = phase;
		this.finalScoreCalculated = finalScoreCalculated;
		this.initialPlayerId = initialPlayerId;
		this.actualPlayerId = actualPlayerId;
		this.matchCreatorId = matchCreatorId;
	}

    public MatchDTO() {}

}