package es.us.dp1.l4_01_24_25.upstream.player;

import org.springframework.stereotype.Component;

import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@Component
public class PlayerMapper {

    private UserService userService;
    private MatchService matchService;

    public static Player toEntity(PlayerDTO dto) {
        Player player = new Player();
        player.setId(dto.getId());
        player.setName(dto.getName());
        player.setColor(dto.getColor());
        player.setPlayerOrder(dto.getPlayerOrder());
        player.setAlive(dto.getAlive());
        player.setPoints(dto.getPoints());
        player.setEnergy(dto.getEnergy());
        if (dto.getUserId() != null) {
            player.setUserPlayer(userService.findById(dto.getUserId()));
        }
        if (dto.getMatchId() != null) {
            player.setMatch(matchService.findById(dto.getMatchId()));
        }
        return player;
    }

    public static PlayerDTO toDTO(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setColor(player.getColor());
        dto.setPlayerOrder(player.getPlayerOrder());
        dto.setAlive(player.getAlive());
        dto.setPoints(player.getPoints());
        dto.setEnergy(player.getEnergy());
        dto.setUserId(player.getUserPlayer() != null ? player.getUserPlayer().getId() : null);
        dto.setMatchId(player.getMatch() != null ? player.getMatch().getId() : null);
        return dto;
    }
}