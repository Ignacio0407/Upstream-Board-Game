package es.us.dp1.l4_01_24_25.upstream.tile;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Tile extends BaseEntity {
    
    String image;
    
    @Enumerated(EnumType.STRING)
	TileType type;
}
