package es.us.dp1.l4_01_24_25.upstream.player;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.partida.Partida;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "player") // -> Esto solo tiene la clase padre
public class Jugador extends BaseEntity implements Serializable{
    
    String name;
    @Enumerated(EnumType.STRING)
    Color color;
    Integer orden;
    Boolean vivo;
    Integer puntos;

    @ManyToOne
    @JoinColumn(name="usuario")
    User usuario;

    
    @JsonSerialize(using = matchSerializer.class)
    @ManyToOne
    @JoinColumn(name="partida")
    Partida partida;


}
