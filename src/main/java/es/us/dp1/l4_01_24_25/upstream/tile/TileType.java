package es.us.dp1.l4_01_24_25.upstream.tile;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tile_type")
public class TileType extends BaseEntity {
    @Column(length = 20)
	String type;
}
