package es.us.dp1.l4_01_24_25.upstream.partida;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.model.NamedEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Jugador;
import es.us.dp1.l4_01_24_25.upstream.player.UserDeserializer;
import es.us.dp1.l4_01_24_25.upstream.player.UserSerializer;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "matches", uniqueConstraints= { @UniqueConstraint(columnNames="matches")})
@Inheritance(strategy = InheritanceType.JOINED)
public class Partida extends NamedEntity {

	// Esto para unirse
	String contrasena;
	@Enumerated(EnumType.STRING)
	Estado estado;
	// Esto para jugar en sí
	@Min(0)
	@Max(5)
	Integer numjugadores;
    Integer ronda;
    @Enumerated(EnumType.STRING)
	Fase fase;
	
	@OneToOne
	@JoinColumn(name="jugador_inicial")
	@JsonSerialize(using = PlayerSerializer.class)
	@JsonDeserialize(using = PlayerDeserializer.class)
	Jugador jugadorinicial;
	
	@OneToOne
	@JoinColumn(name="jugador_actual")
	@JsonSerialize(using = PlayerSerializer.class)
	@JsonDeserialize(using = PlayerDeserializer.class)
	Jugador jugadoractual;

	@OneToOne
	@JoinColumn(name="creador_partida")
	@JsonSerialize(using = UserSerializer.class)
	@JsonDeserialize(using = UserDeserializer.class)
	User creadorpartida;

	public Integer getNumjugadores() {
        return numjugadores;
    }

    // Método setter para el número de jugadores
    public void setNumJugadores(Integer numjugadores) {
        this.numjugadores = numjugadores;
    }

}