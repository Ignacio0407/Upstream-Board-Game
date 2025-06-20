package es.us.dp1.l4_01_24_25.upstream.matchTile;

import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.match.MatchService;
import es.us.dp1.l4_01_24_25.upstream.model.EntityMapper;

public abstract class MatchTileMapper implements EntityMapper<MatchTile, MatchTileDTO>{
 
     @Autowired
    protected MatchService matchService;

    @Override
    @Mapping(source = "match.id", target = "matchId")
    public abstract MatchTileDTO toDTO(MatchTile match);

    @Override
    @Mapping(source = "matchId", target = "match") // needs to resolve the complete User (below)
    public abstract MatchTile toEntity(MatchTileDTO dto);

    protected Match mapMatch(Integer matchId) {
        return matchId == null ? null : matchService.findById(matchId);
    }
}