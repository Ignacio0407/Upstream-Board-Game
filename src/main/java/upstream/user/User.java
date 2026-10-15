package upstream.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import upstream.model.NamedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "appusers")
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends NamedEntity {

	@Column(nullable = false)
	@JsonIgnore
	String password;

	@Enumerated(EnumType.STRING)
	@NotNull
	Authority authority;
	
	Integer victories;
	
	Integer playedgames;
	
	Integer totalpoints;

	public Boolean hasAuthority(Authority auth) {
		return this.getAuthority().toString().toLowerCase().equals(auth.toString().toLowerCase());
	}

}