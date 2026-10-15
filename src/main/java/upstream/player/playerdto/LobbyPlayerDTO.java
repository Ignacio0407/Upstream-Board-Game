package upstream.player.playerdto;

import upstream.player.Color;
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