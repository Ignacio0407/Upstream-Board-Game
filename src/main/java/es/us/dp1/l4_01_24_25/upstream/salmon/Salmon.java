package es.us.dp1.l4_01_24_25.upstream.salmon;
import es.us.dp1.l4_01_24_25.upstream.model.BaseEntity;
import jakarta.persistence.Entity;
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
    Integer color;
    String image;
    
}
