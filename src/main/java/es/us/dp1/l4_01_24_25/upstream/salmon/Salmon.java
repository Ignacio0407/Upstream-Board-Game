package es.us.dp1.l4_01_24_25.upstream.salmon;

import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import es.us.dp1.l4_01_24_25.upstream.player.Color;
import es.us.dp1.l4_01_24_25.upstream.salmonMatch.SalmonMatch;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
@Table(name="salmon")
public class Salmon extends BaseEntity{
    
    @Enumerated(EnumType.STRING)
    Color color;
    String image;

    @OneToOne(mappedBy = "salmon", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "salmonMatch_id")
    SalmonMatch salmonMatch;
    
}
