package es.us.dp1.l4_01_24_25.upstream.salmon;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Color;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="salmon")
public class Salmon extends BaseEntity{
    
    @Enumerated(EnumType.STRING)
    Color color;
    
    String image;
    
}