package es.us.dp1.l4_01_24_25.upstream.partidas;

import es.us.dp1.l4_01_24_25.upstream.model.NamedEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Jugador;
import jakarta.persistence.Entity;
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
public class Partida extends NamedEntity {

	// Esto para unirse
	String contraseña;
	Estado estado;

	// Esto para jugar en sí
	@Min(2)
	@Max(5)
	Integer numJugadores;
    Integer ronda;
    Fase fase; // Esto hay que cambiarlo.
	
	@OneToOne
	@JoinColumn(name="id")
	Jugador jugadorInicial;
	@OneToOne
	@JoinColumn(name="id")
	Jugador jugadorActual;

}

