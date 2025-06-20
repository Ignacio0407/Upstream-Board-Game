package es.us.dp1.l4_01_24_25.upstream.player.playerDTO;

import es.us.dp1.l4_01_24_25.upstream.player.Color;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LobbyPlayerDTO {
    
    private Integer id;

    private String name;

    private Color color;

    private Integer userId;

    private Integer matchId;
}