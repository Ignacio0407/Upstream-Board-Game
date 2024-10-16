package es.us.dp1.l4_01_24_25.upstream.player;

import es.us.dp1.l4_01_24_25.upstream.user.User;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
// @Table(name = "players") -> Esto solo tiene la clase padre
public class Jugador extends User{
    
    Color color;
    Integer orden;
    Boolean vivo;
    Integer puntos;
}
