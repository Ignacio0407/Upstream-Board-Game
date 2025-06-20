package es.us.dp1.l4_01_24_25.upstream.match;

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

}