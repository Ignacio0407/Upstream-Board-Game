package upstream.salmon;

import upstream.model.BaseEntity;
import upstream.player.Color;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Salmon extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    Color color;
    
    String image;
    
}