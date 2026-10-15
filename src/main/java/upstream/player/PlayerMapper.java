package upstream.player;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.model.EntityMapper;
import upstream.player.playerdto.LobbyPlayerDTO;
import upstream.player.playerdto.PlayerDTO;
import upstream.user.User;
import upstream.user.UserService;

@Mapper(componentModel = "spring")
public abstract class PlayerMapper implements EntityMapper<Player, PlayerDTO> {
    
    @Autowired
    @Lazy
    protected UserService userService;

    @Autowired
    @Lazy
    protected MatchService matchService;

    @Override
    @Mapping(source = "userPlayer.id", target = "userId")
    @Mapping(source = "match.id", target = "matchId")
    public abstract PlayerDTO toDTO(Player player);

    @Override
    @Mapping(source = "userId", target = "userPlayer") // needs to resolve the complete User (below)
    @Mapping(source = "matchId", target = "match")
    public abstract Player toEntity(PlayerDTO dto);

    @Mapping(source = "userPlayer.id", target = "userId")
    @Mapping(source = "match.id", target = "matchId")
    public abstract LobbyPlayerDTO toLobby(Player player);

    @Mapping(source = "userId", target = "userPlayer")
    @Mapping(source = "matchId", target = "match")
    @Mapping(target = "alive", ignore = true)
    @Mapping(target = "energy", ignore = true)
    @Mapping(target = "playerOrder", ignore = true)
    @Mapping(target = "points", ignore = true)
    public abstract Player toEntityFromLobby(LobbyPlayerDTO player);

    // Auxiliar methods to mape Integer → Entity
    protected User mapUserPlayer(Integer userId) {
        return userId == null ? null : userService.findById(userId);
    }

    protected Match mapMatch(Integer matchId) {
        return matchId == null ? null : matchService.findById(matchId);
    }

    protected Integer mapUserId(User user) {
    return user != null ? user.getId() : null;
    }

    protected Integer mapMatchId(Match match) {
        return match != null ? match.getId() : null;
    }

}