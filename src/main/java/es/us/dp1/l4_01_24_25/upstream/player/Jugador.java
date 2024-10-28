package es.us.dp1.l4_01_24_25.upstream.player;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "jugador") // -> Esto solo tiene la clase padre
public class Jugador extends BaseEntity{
    
    String name;
    @Enumerated(EnumType.STRING)
    Color color;
    Integer orden;
    Boolean vivo;
    Integer puntos;
}
