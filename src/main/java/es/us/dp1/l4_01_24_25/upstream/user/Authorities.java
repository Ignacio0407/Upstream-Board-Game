package es.us.dp1.l4_01_24_25.upstream.user;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Authorities extends BaseEntity{
	
	@ManyToOne
	@JoinColumn(name = "username")
	User user;
	
	@Column(length = 20)
	String authority;
	
	
}
