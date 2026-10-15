package upstream.tile;

import upstream.model.BaseEntity;
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
