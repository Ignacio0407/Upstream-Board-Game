package upstream.matchtile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import upstream.match.Match;
import upstream.match.MatchService;
import upstream.matchtile.dto.MatchTileDTO;
import upstream.model.EntityMapper;

@Mapper(componentModel = "spring")
public abstract class MatchTileMapper implements EntityMapper<MatchTile, MatchTileDTO>{
 
    @Autowired
    @Lazy
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