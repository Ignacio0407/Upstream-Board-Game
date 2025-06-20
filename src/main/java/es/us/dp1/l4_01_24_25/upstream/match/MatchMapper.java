package es.us.dp1.l4_01_24_25.upstream.match;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import es.us.dp1.l4_01_24_25.upstream.model.EntityMapper;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;
import es.us.dp1.l4_01_24_25.upstream.user.User;
import es.us.dp1.l4_01_24_25.upstream.user.UserService;

@Mapper(componentModel = "spring")
public abstract class MatchMapper implements EntityMapper<Match, MatchDTO>{
 
     @Autowired
    protected UserService userService;

    @Autowired
    protected PlayerService playerService;

    @Override
    @Mapping(source = "initialPlayer.id", target = "initialPlayerId")
    @Mapping(source = "actualPlayer.id", target = "actualPlayerId")
    @Mapping(source = "matchCreator.id", target = "matchCreatorId")
    public abstract MatchDTO toDTO(Match match);

    @Override
    @Mapping(source = "initialPlayerId", target = "initialPlayer") // needs to resolve the complete User (below)
    @Mapping(source = "actualPlayerId", target = "actualPlayer")
    @Mapping(source = "matchCreatorId", target = "matchCreator", qualifiedByName = "mapMatchCreator")
    public abstract Match toEntity(MatchDTO dto);

    // Auxiliar methods to mape Integer → Entity
    protected Player mapPlayer(Integer playerId) {
        return playerId == null ? null : playerService.findById(playerId);
    }

    @Named("mapMatchCreator")
    protected User mapMatchCreator(Integer userId) {
        return userId == null ? null : userService.findById(userId);
    }
    
}