package es.us.dp1.l4_01_24_25.upstream.partidaCasilla;

import es.us.dp1.l4_01_24_25.upstream.coordenada.Coordenada;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name="partidaCasilla")
public class PartidaCasilla extends BaseEntity {

    Integer orientacion;
    Integer capacidad;
    Coordenada coordenada;


    
}
