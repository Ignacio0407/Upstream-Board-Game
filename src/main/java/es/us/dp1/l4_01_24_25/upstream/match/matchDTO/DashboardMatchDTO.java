package es.us.dp1.l4_01_24_25.upstream.match.matchDTO;

import es.us.dp1.l4_01_24_25.upstream.match.State;
import es.us.dp1.l4_01_24_25.upstream.validation.ValidNumber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardMatchDTO {
    
    Integer id;

	String name;
	
	String password;
	
	State state;
	
	@ValidNumber(min=0,max=5)
	Integer playersNumber;

	public DashboardMatchDTO(Integer id, String name, String password, State state, Integer playersNumber) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.state = state;
		this.playersNumber = playersNumber;
	}

}