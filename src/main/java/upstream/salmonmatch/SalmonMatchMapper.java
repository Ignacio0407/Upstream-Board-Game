package upstream.salmonmatch;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.model.EntityMapper;
import upstream.player.Player;
import upstream.player.PlayerService;
import upstream.salmonmatch.dto.SalmonMatchDTO;

@Mapper(componentModel = "spring")
public abstract class SalmonMatchMapper implements EntityMapper<SalmonMatch, SalmonMatchDTO> {
    
    @Autowired
    @Lazy
    protected PlayerService playerService;

    @Autowired
    @Lazy
    protected MatchService matchService;

    @Override
    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "match.id", target = "matchId")
    public abstract SalmonMatchDTO toDTO(SalmonMatch player);

    @Override
    @Mapping(source = "playerId", target = "player") // needs to resolve the complete User (below)
    @Mapping(source = "matchId", target = "match")
    public abstract SalmonMatch toEntity(SalmonMatchDTO dto);

    // Auxiliar methods to mape Integer → Entity
    protected Player mapPlayer(Integer playerId) {
        return playerId == null ? null : playerService.findById(playerId);
    }

    protected Match mapMatch(Integer matchId) {
        return matchId == null ? null : matchService.findById(matchId);
    }

}