package es.us.dp1.l4_01_24_25.upstream.player.playerDTO;

import es.us.dp1.l4_01_24_25.upstream.player.Color;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerDTO {
    
    private Integer id;

    private String name;

    private Color color;

    private Integer playerOrder;

    private Boolean alive;

    private Integer points;

    private Integer energy;

    private Integer userId;

    private Integer matchId;

    public PlayerDTO(Integer id, String name, Color color, Integer playerOrder, Boolean alive, Integer points, Integer energy, Integer userId, Integer matchId) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.playerOrder = playerOrder;
        this.alive = alive;
        this.points = points;
        this.energy = energy;
        this.userId = userId;
        this.matchId = matchId;
    }
    
}