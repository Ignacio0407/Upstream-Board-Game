package es.us.dp1.l4_01_24_25.upstream.chat;

import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.EntityMapper;
import es.us.dp1.l4_01_24_25.upstream.player.Player;
import es.us.dp1.l4_01_24_25.upstream.player.PlayerService;

public abstract class MessageMapper implements EntityMapper<Message, MessageDTO> {
    
    @Autowired
    protected PlayerService playerService;

    @Autowired
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