package es.us.dp1.l4_01_24_25.upstream.statistic;

import es.us.dp1.l4_01_24_25.upstream.model.NamedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Achievement extends NamedEntity {
    
    private String description;
    
    private String badgeImage;
    
    @Min(0)
    private Double threshold;

    @Enumerated(EnumType.STRING)
    @NotNull
    Metric metric;

}
