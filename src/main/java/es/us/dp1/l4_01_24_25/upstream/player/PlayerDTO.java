package es.us.dp1.l4_01_24_25.upstream.player;

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

}