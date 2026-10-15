package upstream.chat;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.model.EntityMapper;
import upstream.player.Player;
import upstream.player.PlayerService;

@Mapper(componentModel = "spring")
public abstract class MessageMapper implements EntityMapper<Message, MessageDTO> {
    
    @Autowired
    @Lazy
    protected PlayerService playerService;

    @Autowired
    @Lazy
    protected MatchService matchService;

    @Override
    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "match.id", target = "matchId")
    @Mapping(source = "player.name", target = "playerName")
    public abstract MessageDTO toDTO(Message message);

    @Override
    @Mapping(source = "playerId", target = "player") // needs to resolve the complete User (below)
    @Mapping(source = "matchId", target = "match")
    @Mapping(target = "createdAt", ignore = true)
    public abstract Message toEntity(MessageDTO dto);

    // Auxiliar methods to mape Integer → Entity
    protected Player mapPlayer(Integer playerId) {
        return playerId == null ? null : playerService.findById(playerId);
    }

    protected Match mapMatch(Integer matchId) {
        return matchId == null ? null : matchService.findById(matchId);
    }
    
}