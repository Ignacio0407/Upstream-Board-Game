package es.us.dp1.l4_01_24_25.upstream.partida;

import es.us.dp1.l4_01_24_25.upstream.model.NamedEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Jugador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	String contrasena;
	@Enumerated(EnumType.STRING)
	Estado estado;
	// Esto para jugar en sí
	@Min(2)
	@Max(5)
	@Column(name = "num_jugadores")
	Integer numJugadores;
    Integer ronda;
    @Enumerated(EnumType.STRING)
	Fase fase;
	
	@OneToOne
	@JoinColumn(name="jugador_inicial")
	Jugador jugadorInicial;
	@OneToOne
	@JoinColumn(name="jugador_actual")
	Jugador jugadorActual;

}