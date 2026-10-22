package upstream.matchtile;

import upstream.coordinate.Coordinate;
import upstream.match.Match;
import upstream.model.BaseEntity;
import upstream.tile.Tile;
import upstream.validation.ValidNumber;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="match_tile")
public class MatchTile extends BaseEntity {

    @ValidNumber(min=0, max=5)
    Integer capacity;

    @ValidNumber(min=0)
    Integer orientation;

    @ValidNumber(min=0, max=5)
    Integer salmonsNumber;

    @Embedded
    @Valid
    Coordinate coordinate;

    @ManyToOne
    Tile tile;

    @ManyToOne
    Match match;

    public boolean isFull () {
        return this.getSalmonsNumber() >= this.getCapacity();
    }

}
