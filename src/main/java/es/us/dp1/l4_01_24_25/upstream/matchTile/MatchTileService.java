package es.us.dp1.l4_01_24_25.upstream.matchTile;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.us.dp1.l4_01_24_25.upstream.general.BaseService;
import es.us.dp1.l4_01_24_25.upstream.match.Match;
import es.us.dp1.l4_01_24_25.upstream.tile.Tile;
import es.us.dp1.l4_01_24_25.upstream.tile.TileService;

@Service
public class MatchTileService extends BaseService<MatchTile,Integer>{
    
    MatchTileRepository matchTileRepository;
    TileService tileService;

    @Autowired
    public MatchTileService(MatchTileRepository matchTileRepository, TileService tileService) {
        super(matchTileRepository);
        this.tileService = tileService;
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findByMatchId(Integer matchId) {
        List<MatchTile> res = matchTileRepository.findByMatchId(matchId);
        if(res == null) res = List.of();
        return res;
    }

    @Transactional(readOnly = true)
    public List<MatchTile> findByMatchIdNoCoord(Integer matchId) {
        return matchTileRepository.findWithNoCoord(matchId);
    }

    @Transactional(readOnly = true)
    public MatchTile findByCoordinate(Integer x, Integer y) {
        return matchTileRepository.findByCoordinate(x,y);
    }

    public Boolean validateTilePlacement(Integer round, Integer y) {
        int maxAllowedRow = round - 1;  
        return y <= maxAllowedRow;
    }
    
    public MatchTile eagleToWater(MatchTile toTravel, Match match) {
        MatchTile agua = new MatchTile();
        Tile aguaTile = tileService.findById(1);
        agua.setId(toTravel.getId());
        agua.setCapacity(toTravel.getCapacity());
        agua.setOrientation(0);
        agua.setSalmonsNumber(0);
        agua.setCoordinate(toTravel.getCoordinate());
        agua.setTile(aguaTile);
        agua.setMatch(match);
        matchTileRepository.save(agua);
        return agua;
    }
}