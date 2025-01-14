package es.us.dp1.l4_01_24_25.upstream.tile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tile")
public class Tile extends BaseEntity {
    String image;
    
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "tile_type")
    @JsonSerialize(using = TileTypeSerializer.class)
    @JsonDeserialize(using = TileTypeDeserializer.class)
    TileType type;
}
